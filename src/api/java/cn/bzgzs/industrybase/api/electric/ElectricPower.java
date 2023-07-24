package cn.bzgzs.industrybase.api.electric;

import cn.bzgzs.industrybase.api.CapabilityList;
import cn.bzgzs.industrybase.api.energy.IElectricPower;
import cn.bzgzs.industrybase.api.network.ApiNetworkManager;
import cn.bzgzs.industrybase.api.network.client.UnsubscribeWireConnPacket;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.IEnergyStorage;

import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

public class ElectricPower implements IElectricPower {
	private double tmpOutputPower;
	private double tmpInputPower;
	private final Set<BlockPos> tmpConn;
	private final BlockEntity blockEntity;
	private final BlockPos pos;
	@Nullable
	private ElectricNetwork network;
	private final LazyOptional<IElectricPower> EPOptional;
	private final LazyOptional<IEnergyStorage> FEOptional;

	public ElectricPower(BlockEntity blockEntity) {
		this.blockEntity = blockEntity;
		this.pos = blockEntity.getBlockPos();
		this.tmpConn = new HashSet<>();
		this.EPOptional = LazyOptional.of(() -> this);
		this.FEOptional = LazyOptional.of(ForgeEnergy::new);
	}

	public <X> LazyOptional<X> cast(Capability<X> cap, LazyOptional<X> defaultCap) {
		if (cap == CapabilityList.ELECTRIC_POWER) {
			return this.EPOptional.cast();
		} else if (cap == ForgeCapabilities.ENERGY) {
			return this.FEOptional.cast();
		} else {
			return defaultCap;
		}
	}

	/**
	 * 向电力网络注册该方块。
	 * 需要在 {@link BlockEntity#onLoad()} 中执行一次。
	 */
	public void register() {
		Optional.ofNullable(this.blockEntity.getLevel()).ifPresent(level -> {
			this.network = ElectricNetwork.Manager.get(level);
			if (!level.isClientSide) {
				this.setOutputPower(this.tmpOutputPower);
				this.setInputPower(this.tmpInputPower);
				this.network.addOrChangeBlock(this.pos, this.blockEntity::setChanged);
				this.tmpConn.forEach(toPos -> this.network.addWire(this.pos, toPos, () -> {
				}));
			}
		});
	}

	/**
	 * 将此方块从电力网络中移除。
	 * 在 {@link BlockEntity#setRemoved()} 中执行。
	 */
	public void remove() {
		Optional.ofNullable(this.blockEntity.getLevel()).ifPresent(level -> {
			if (this.network != null) {
				if (level.isClientSide) {
					ApiNetworkManager.INSTANCE.sendToServer(new UnsubscribeWireConnPacket(this.pos));
					this.network.removeClientWires(this.pos);
				} else {
					this.network.removeBlock(this.pos, this.blockEntity::setChanged);
				}
			}
		});
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
		double diff = this.network.setMachineOutput(this.pos, power);
		if (diff != 0) this.blockEntity.setChanged();
		return diff;
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
		double diff = this.network.setMachineInput(this.pos, power);
		if (diff != 0) this.blockEntity.setChanged();
		return diff;
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
	public boolean canExtract() {
		return this.getOutputPower() > 0.0D;
	}

	@Override
	public boolean canReceive() {
		return this.getInputPower() > 0.0D;
	}

	@CanIgnoreReturnValue
	public ElectricPower readFromNBT(CompoundTag tag) {
		CompoundTag nbt = tag.getCompound("ElectricPower");
		this.tmpOutputPower = nbt.getDouble("Output");
		this.tmpInputPower = nbt.getDouble("Input");
		nbt.getList("Connections", Tag.TAG_COMPOUND).forEach(entry -> this.tmpConn.add(NbtUtils.readBlockPos((CompoundTag) entry)));
		return this;
	}

	@CanIgnoreReturnValue
	public CompoundTag writeToNBT(CompoundTag tag) {
		CompoundTag nbt = new CompoundTag();
		ListTag listTag = new ListTag();
		if (this.network != null) { // TODO 1.19.3 可能不用
			nbt.putDouble("Output", this.getOutputPower());
			nbt.putDouble("Input", this.getInputPower());
			this.network.wireConnects(this.pos).forEach(pos -> listTag.add(NbtUtils.writeBlockPos(pos)));
			nbt.put("Connections", listTag);
		} else {
			nbt.putDouble("Output", this.tmpOutputPower);
			nbt.putDouble("Input", this.tmpInputPower);
			this.tmpConn.forEach(pos -> listTag.add(NbtUtils.writeBlockPos(pos)));
			nbt.put("Connections", listTag);
		}
		tag.put("ElectricPower", nbt);
		return tag;
	}

	private class ForgeEnergy implements IEnergyStorage {
		private final ElectricNetwork network = ElectricPower.this.network;
		private final BlockPos pos = ElectricPower.this.pos;

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
}
