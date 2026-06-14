package net.industrybase.api.electric;

import net.industrybase.api.energy.IElectricPower;
import net.industrybase.api.network.client.UnsubscribeWireConnPacket;
import net.industrybase.api.util.NbtHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.*;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import net.neoforged.neoforge.common.util.ValueIOSerializable;
import net.neoforged.neoforge.transfer.TransferPreconditions;
import net.neoforged.neoforge.transfer.energy.EnergyHandler;
import net.neoforged.neoforge.transfer.transaction.SnapshotJournal;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

import org.jspecify.annotations.Nullable;

import java.util.HashSet;
import java.util.Set;

public class ElectricPower implements IElectricPower, EnergyHandler, ValueIOSerializable {
	private double tmpOutputPower;
	private double tmpInputPower;
	private Set<BlockPos> tmpConn;
	@Nullable
	private Level level;
	private final BlockPos pos;
	private final BlockEntity blockEntity;
	@Nullable
	private ElectricNetwork network;
    private final EnergyJournal energyJournal = new EnergyJournal();
    private int tmpFE = 0;

	public ElectricPower(BlockEntity blockEntity) {
		this.blockEntity = blockEntity;
		this.pos = blockEntity.getBlockPos();
		this.tmpConn = new HashSet<>(0);
	}

	/**
	 * 向电力网络注册该方块。
	 * 需要在 {@link BlockEntity#onLoad()} 中执行一次。
	 */
	public void register() {
		this.level = this.blockEntity.getLevel();
		if (this.level != null) {
			this.network = ElectricNetwork.Manager.get(this.level);
			if (!this.level.isClientSide()) {
				this.setOutputPower(this.tmpOutputPower);
				this.setInputPower(this.tmpInputPower);
				this.network.addOrChangeBlock(this.pos, this.blockEntity::setChanged);
				this.tmpConn.forEach(toPos -> this.network.addWire(this.pos, toPos, () -> {
				}));
			}
		}
	}

	/**
	 * 将此方块从电力网络中移除。
	 * 在 {@link BlockEntity#setRemoved()} 中执行。
	 */
	public void remove() {
		if (this.level != null) {
			if (this.network != null) {
				if (level.isClientSide()) {
					ClientPacketDistributor.sendToServer(new UnsubscribeWireConnPacket(this.pos));
					this.network.removeClientWires(this.pos);
				} else {
					this.network.removeBlock(this.pos, this.blockEntity::setChanged);
				}
			}
		}
	}

	@Nullable
	public ElectricNetwork getNetwork() {
		return this.network;
	}

	@Override
	public double getOutputPower() {
		return this.network.getMachineOutput(this.pos);
	}

	/**
	 * 设置方块的输出功率。
	 *
	 * @param power 要设置的输出功率
	 * @return 原功率与新设功率的差值
	 */
	@Override
	public double setOutputPower(double power) {
		if (!this.level.isClientSide()) {
			double diff = this.network.setMachineOutput(this.pos, power);
			if (diff != 0) this.blockEntity.setChanged();
			return diff;
		}
		return 0;
	}

	/**
	 * 获取方块额定输入功率。
	 *
	 * @return 额定输入功率
	 */
	@Override
	public double getInputPower() {
		return this.network.getMachineInput(this.pos);
	}

	@Override
	public double setInputPower(double power) {
		if (!this.level.isClientSide()) {
			double diff = this.network.setMachineInput(this.pos, power);
			if (diff != 0) this.blockEntity.setChanged();
			return diff;
		}
		return 0;
	}

	/**
	 * 获取方块实际获得的输入功率。
	 *
	 * @return 实际输入功率
	 */
	@Override
	public double getRealInput() {
		return this.network.getRealInput(this.pos);
	}

    @Override
    public void serialize(ValueOutput output) {
        if (this.network != null) {
            // if registered (block entity loaded), save newer values
            this.tmpOutputPower = this.getOutputPower();
            this.tmpInputPower = this.getInputPower();
            this.tmpConn = this.network.getWireConn(this.pos);
        }

        // if not registered, save old values (probably from before unloading)
        output.putDouble("Output", this.tmpOutputPower);
        output.putDouble("Input", this.tmpInputPower);

        var list = output.list("Connections", ExtraCodecs.NBT);
        this.tmpConn.forEach(pos -> list.add(NbtHelper.writeBlockPos(pos)));
    }

    @Override
    public void deserialize(ValueInput input) {
        this.tmpOutputPower = input.getDoubleOr("Output", 0.0);
        this.tmpInputPower = input.getDoubleOr("Input", 0.0);

        // create new set to avoid modifying the original one
        HashSet<BlockPos> connections = new HashSet<>();
        input.listOrEmpty("Connections", ExtraCodecs.NBT).forEach(entry -> {
            if (entry instanceof IntArrayTag tag)
                NbtHelper.readBlockPos(tag).ifPresent(connections::add);
        });
        this.tmpConn = connections;
    }

    @Override
    public long getAmountAsLong() {
        return this.network.getFEEnergy(this.pos);
    }

    @Override
    public long getCapacityAsLong() {
        return this.network.getMaxFEStored(this.pos);
    }

    @Override
    public int insert(int amount, TransactionContext transaction) {
        TransferPreconditions.checkNonNegative(amount);

        int inserted = Math.min(this.getCapacityAsInt() - this.getAmountAsInt(), amount);
        if (inserted > 0) {
            energyJournal.updateSnapshots(transaction);
            tmpFE += inserted;
            return inserted;
        }
        return 0;
    }

    @Override
    public int extract(int amount, TransactionContext transaction) {
        TransferPreconditions.checkNonNegative(amount);

        int extracted = Math.min(this.getAmountAsInt(), amount);
        if (extracted > 0) {
            energyJournal.updateSnapshots(transaction);
            tmpFE -= extracted;
            return extracted;
        }
        return 0;
    }

    private class EnergyJournal extends SnapshotJournal<Integer> {
        @Override
        protected Integer createSnapshot() {
            return tmpFE;
        }

        @Override
        protected void revertToSnapshot(Integer snapshot) {
            tmpFE = snapshot;
        }

        @Override
        protected void onRootCommit(Integer originalState) {
            int previousAmount = originalState;
            if (tmpFE > previousAmount) {
                network.receiveFEEnergy(pos, tmpFE - previousAmount, false);
            } else if (tmpFE < previousAmount) {
                network.extractFEEnergy(pos, previousAmount - tmpFE, false);
            }
            tmpFE = 0;
        }
    }
}
