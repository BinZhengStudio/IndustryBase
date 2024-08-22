package net.industrybase.world.level.block.entity;

import net.industrybase.api.pipe.PipeConnectedHandler;
import net.industrybase.api.pipe.StorageInterface;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.NeoForgeMod;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;

public class FluidTankBlockEntity extends BlockEntity {
	private final PipeConnectedHandler handler = new PipeConnectedHandler(this);
	private final FluidTank tank = new FluidTank(10000, fluidStack -> fluidStack.is(NeoForgeMod.WATER_TYPE.value())) {
		@Override
		protected void onContentsChanged() {
			for (Direction direction : Direction.values()) {
				FluidTankBlockEntity.this.handler.setPressure(direction, (double) getFluidAmount() / getCapacity());
			}
		}
	};

	public FluidTankBlockEntity(BlockPos pos, BlockState blockState) {
		super(BlockEntityTypeList.FLUID_TANK.get(), pos, blockState);
	}

	@Override
	public void onLoad() {
		super.onLoad();
		this.handler.registerHandler(new StorageInterface(this.tank::getCapacity, this.tank::getFluidAmount, this.tank::fill));
	}

	public FluidTank getTank(Direction direction) {
		return this.tank;
	}

	@Override
	public void setRemoved() {
		this.handler.removeHandler();
		super.setRemoved();
	}
}
