package net.industrybase.world.level.block.entity;

import net.industrybase.api.pipe.PipeConnectedHandler;
import net.industrybase.api.pipe.StorageInterface;
import net.industrybase.network.server.WaterAmountPayload;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.NeoForgeMod;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import net.neoforged.neoforge.network.PacketDistributor;

public class FluidTankBlockEntity extends BlockEntity {
	public static final int CAPACITY = 8000;
	private int waterAmount;
	private final PipeConnectedHandler handler = new PipeConnectedHandler(this);
	private final FluidTank tank = new FluidTank(CAPACITY, fluidStack -> fluidStack.is(NeoForgeMod.WATER_TYPE.value())) {
		@Override
		protected void onContentsChanged() {
			if (level != null && !level.isClientSide) {
				PacketDistributor.sendToAllPlayers(new WaterAmountPayload(worldPosition, tank.getFluidAmount()));
				for (Direction direction : Direction.values()) {
					handler.setPressure(direction, (double) this.getFluidAmount() / this.getCapacity());
				}
			}
		}
	};

	public FluidTankBlockEntity(BlockPos pos, BlockState blockState) {
		super(BlockEntityTypeList.FLUID_TANK.get(), pos, blockState);
	}

	@Override
	public void onLoad() {
		super.onLoad();
		this.handler.registerHandler(new StorageInterface(this.tank::getCapacity, this.tank::getFluidAmount, this.tank::fill, this.tank::drain));
	}

	public FluidTank getTank(Direction direction) {
		return this.tank;
	}

	public int getWaterAmount() {
		return this.waterAmount;
	}

	public void setClientWaterAmount(int waterAmount) {
		this.waterAmount = waterAmount;
	}

	@Override
	public void setRemoved() {
		this.handler.removeHandler();
		super.setRemoved();
	}
}
