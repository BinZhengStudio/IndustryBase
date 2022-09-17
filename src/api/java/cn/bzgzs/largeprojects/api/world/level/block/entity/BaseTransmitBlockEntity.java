package cn.bzgzs.largeprojects.api.world.level.block.entity;

import cn.bzgzs.largeprojects.api.CapabilityList;
import cn.bzgzs.largeprojects.api.energy.IMechanicalTransmit;
import cn.bzgzs.largeprojects.api.util.TransmitNetwork;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class BaseTransmitBlockEntity extends BlockEntity {
	@Nullable
	private TransmitNetwork network;
	private int tmpPower;
	private int tmpResistance;
	private final LazyOptional<IMechanicalTransmit> transmit = LazyOptional.of(() -> new IMechanicalTransmit() {
		@Override
		public int getPower() {
			return BaseTransmitBlockEntity.this.getPower();
		}

		@Override
		public int getResistance() {
			return BaseTransmitBlockEntity.this.getResistance();
		}

		@Override
		public double getSpeed() {
			return BaseTransmitBlockEntity.this.getSpeed();
		}

		@Override
		public boolean canExtract() {
			return BaseTransmitBlockEntity.this.canExtract();
		}

		@Override
		public boolean canReceive() {
			return BaseTransmitBlockEntity.this.canReceive();
		}
	});

	public BaseTransmitBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
	}

	public int getPower() { // TODO overrides
		return this.getNetwork().getMachinePower(this.worldPosition);
	}

	protected int setPower(int power) {
		int diff = this.network.setMachinePower(this.worldPosition, power);
		if (diff != 0) {
			this.setChanged();
		}
		return diff;
	}

	public int getResistance() { // TODO overrides
		return this.network.getMachineResistance(this.worldPosition);
	}

	protected int setResistance(int resistance) {
		int diff = this.network.setMachineResistance(this.worldPosition, resistance);
		if (diff != 0) {
			this.setChanged();
		}
		return diff;
	}

	public double getSpeed() {
		return this.network.speed(this.worldPosition);
	}

	public abstract boolean canExtract();

	public abstract boolean canReceive();

	public LazyOptional<IMechanicalTransmit> getTransmit() {
		return this.transmit;
	}

	public TransmitNetwork getNetwork() {
		return this.network;
	}

	@Override
	public void load(CompoundTag tag) {
		super.load(tag);
		this.tmpPower = tag.getInt("MechanicalPower");
		this.tmpResistance = tag.getInt("MechanicalResistance");
	}

	@Override
	protected void saveAdditional(CompoundTag tag) {
		super.saveAdditional(tag);
		tag.putInt("MechanicalPower", this.network.getMachinePower(this.worldPosition));
		tag.putInt("MechanicalResistance", this.network.getMachineResistance(this.worldPosition));
	}

	@Override
	public void setLevel(@NotNull Level level) {
		super.setLevel(level);
		this.network = TransmitNetwork.Manager.get(level);
	}

	@Override
	public void onLoad() {
		if (this.level != null && !this.level.isClientSide) {
			this.setPower(this.tmpPower);
			this.setResistance(this.tmpResistance);
			network.addOrChangeBlock(this.worldPosition, this::setChanged);
		}
		super.onLoad();
	}

	@Override
	public void onChunkUnloaded() {
		if (this.level != null && !this.level.isClientSide) {
			network.removeBlock(this.worldPosition, this::setChanged);
		}
		super.onChunkUnloaded();
	}

	@Override
	public void setRemoved() {
		if (this.level != null && !this.level.isClientSide) {
			network.removeBlock(this.worldPosition, this::setChanged);
		}
		super.setRemoved();
	}

	@Override
	public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap) { // TODO 可能不用
		return cap == CapabilityList.MECHANICAL_TRANSMIT ? this.transmit.cast() : super.getCapability(cap);
	}
}
