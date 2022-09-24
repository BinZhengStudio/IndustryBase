package cn.bzgzs.industrybase.api.world.level.block.entity;

import cn.bzgzs.industrybase.api.energy.IMechanicalTransmit;
import cn.bzgzs.industrybase.api.util.TransmitNetwork;
import cn.bzgzs.industrybase.api.CapabilityList;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;

public abstract class ContainerTransmitBlockEntity extends BaseContainerBlockEntity { // TODO network
	private TransmitNetwork network;
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

		@Override
		public boolean canExtract() {
			return ContainerTransmitBlockEntity.this.canExtract();
		}

		@Override
		public boolean canReceive() {
			return ContainerTransmitBlockEntity.this.canReceive();
		}
	});

	public ContainerTransmitBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
		this.network = TransmitNetwork.Manager.get(this.level);
	}

	public abstract int getPower();

	public abstract int getResistance();

	public abstract double getSpeed();

	public abstract boolean canExtract();

	public abstract boolean canReceive();

	public LazyOptional<IMechanicalTransmit> getTransmit() {
		return this.transmit;
	}

	public TransmitNetwork getNetwork() {
		return this.network;
	}

	@Override
	public void setLevel(@NotNull Level level) {
		super.setLevel(level);
		this.network = TransmitNetwork.Manager.get(this.level);
	}

	@Override
	public void onLoad() {
		if (this.level != null && !this.level.isClientSide) {
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
	public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap) {
		return cap == CapabilityList.MECHANICAL_TRANSMIT ? this.transmit.cast() : super.getCapability(cap);
	}
}
