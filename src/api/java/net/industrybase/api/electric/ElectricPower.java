package net.industrybase.api.electric;

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import net.industrybase.api.energy.IElectricPower;
import net.industrybase.api.network.client.UnsubscribeWireConnPacket;
import net.industrybase.api.util.NbtHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.common.util.INBTSerializable;
import net.neoforged.neoforge.energy.IEnergyStorage;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Set;

public class ElectricPower implements IElectricPower, IEnergyStorage, INBTSerializable<CompoundTag> {
	private double tmpOutputPower;
	private double tmpInputPower;
	private final Set<BlockPos> tmpConn;
	@Nullable
	private Level level;
	private final BlockPos pos;
	private final BlockEntity blockEntity;
	@Nullable
	private ElectricNetwork network;

	public ElectricPower(BlockEntity blockEntity) {
		this.blockEntity = blockEntity;
		this.pos = blockEntity.getBlockPos();
		this.tmpConn = new HashSet<>();
	}

	/**
	 * 向电力网络注册该方块。
	 * 需要在 {@link BlockEntity#onLoad()} 中执行一次。
	 */
	public void register() {
		this.level = this.blockEntity.getLevel();
		if (this.level != null) {
			this.network = ElectricNetwork.Manager.get(this.level);
			if (!this.level.isClientSide) {
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
				if (level.isClientSide) {
					PacketDistributor.sendToServer(new UnsubscribeWireConnPacket(this.pos));
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
		if (!this.level.isClientSide) {
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
		if (!this.level.isClientSide) {
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
	public CompoundTag serializeNBT(@Nullable HolderLookup.Provider provider) {
		CompoundTag nbt = new CompoundTag();
		ListTag listTag = new ListTag();
		nbt.putDouble("Output", this.getOutputPower());
		nbt.putDouble("Input", this.getInputPower());
		this.network.getWireConn(this.pos).forEach(pos -> listTag.add(NbtUtils.writeBlockPos(pos)));
		nbt.put("Connections", listTag);
		return nbt;
	}

	@Override
	public void deserializeNBT(@Nullable HolderLookup.Provider provider, CompoundTag nbt) {
		this.tmpOutputPower = nbt.getDouble("Output");
		this.tmpInputPower = nbt.getDouble("Input");
		nbt.getList("Connections", Tag.TAG_INT_ARRAY).forEach(entry ->
				NbtHelper.readBlockPos((IntArrayTag) entry).ifPresent(this.tmpConn::add));
	}

	public void readFromNBT(CompoundTag tag) {
		CompoundTag nbt = tag.getCompound("ElectricPower");
		this.deserializeNBT(null, nbt);
	}

	@CanIgnoreReturnValue
	public CompoundTag writeToNBT(CompoundTag tag) {
		CompoundTag nbt = new CompoundTag();
		ListTag listTag = new ListTag();
		if (this.network != null) {
			nbt.putDouble("Output", this.getOutputPower());
			nbt.putDouble("Input", this.getInputPower());
			this.network.getWireConn(this.pos).forEach(pos -> listTag.add(NbtUtils.writeBlockPos(pos)));
			nbt.put("Connections", listTag);
		}
		tag.put("ElectricPower", nbt);
		return tag;
	}

	@Override
	public int receiveEnergy(int maxReceive, boolean simulate) {
		return this.network.receiveFEEnergy(this.pos, maxReceive, simulate);
	}

	@Override
	public int extractEnergy(int maxExtract, boolean simulate) {
		return this.network.extractFEEnergy(this.pos, maxExtract, simulate);
	}

	@Override
	public int getEnergyStored() {
		return this.network.getFEEnergy(this.pos);
	}

	@Override
	public int getMaxEnergyStored() {
		return this.network.getMaxFEStored(this.pos);
	}

	@Override
	public boolean canExtract() {
		return this.network.getFEEnergy(this.pos) > 0;
	}

	@Override
	public boolean canReceive() {
		return this.network.getFEEnergy(this.pos) < this.getMaxEnergyStored();
	}
}
