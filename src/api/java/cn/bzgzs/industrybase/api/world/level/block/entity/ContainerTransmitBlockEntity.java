package cn.bzgzs.industrybase.api.world.level.block.entity;

import cn.bzgzs.industrybase.api.energy.IMechanicalTransmit;
import cn.bzgzs.industrybase.api.util.TransmitNetwork;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class ContainerTransmitBlockEntity extends BaseContainerBlockEntity {
	@Nullable
	private TransmitNetwork network;
	private int tmpPower;
	private int tmpResistance;
	private final LazyOptional<IMechanicalTransmit> transmit = LazyOptional.of(() -> new IMechanicalTransmit() {
		@Override
		public int getPower() {
			return ContainerTransmitBlockEntity.this.getPower();
		}

		@Override
		public int getResistance() {
			return ContainerTransmitBlockEntity.this.getResistance();
		}

		@Override
		public double getSpeed() {
			return ContainerTransmitBlockEntity.this.getSpeed();
		}
	});

	public ContainerTransmitBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
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
			this.setPower(0);
			this.setResistance(0);
			network.removeBlock(this.worldPosition, this::setChanged);
		}
		super.onChunkUnloaded();
	}

	@Override
	public void setRemoved() {
		if (this.level != null && !this.level.isClientSide) {
			this.setPower(0);
			this.setResistance(0);
			network.removeBlock(this.worldPosition, this::setChanged);
		}
		super.setRemoved();
	}
}
