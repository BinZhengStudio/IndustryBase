package cn.bzgzs.largeprojects.world.level.block.entity;

import cn.bzgzs.largeprojects.api.CapabilityList;
import cn.bzgzs.largeprojects.api.energy.IMechanicalTransmit;
import cn.bzgzs.largeprojects.api.energy.TransmitNetwork;
import cn.bzgzs.largeprojects.api.world.level.block.entity.BaseTransmitBlockEntity;
import cn.bzgzs.largeprojects.world.level.block.TransmissionRodBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TransmissionRodBlockEntity extends BaseTransmitBlockEntity {
	private final LazyOptional<IMechanicalTransmit> transmit = LazyOptional.of(() -> new IMechanicalTransmit() {
		private final TransmitNetwork network = TransmitNetwork.Factory.get(TransmissionRodBlockEntity.this.level);

		@Override
		public int getPower() {
			return 0;
		}

		@Override
		public int getResistance() {
			return 0;
		}

		@Override
		public double getSpeed() {
			return this.network.speed(TransmissionRodBlockEntity.this.worldPosition);
		}

		@Override
		public boolean canExtract() { // TODO
			return false;
		}

		@Override
		public boolean canReceive() { // TODO
			return false;
		}
	});

	public TransmissionRodBlockEntity(BlockPos pos, BlockState state) {
		super(BlockEntityTypeList.TRANSMISSION_ROD.get(), pos, state);
	}

	@Override
	public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
		if (side != null && side.getAxis() == this.getBlockState().getValue(TransmissionRodBlock.AXIS)) {
			return cap == CapabilityList.MECHANICAL_TRANSMIT ? this.transmit.cast() : super.getCapability(cap, side);
		}
		return super.getCapability(cap, side);
	}
}
