package net.industrybase.world.level.block.entity;

import net.industrybase.api.pipe.PipeConnectedHandler;
import net.industrybase.api.pipe.StorageInterface;
import net.industrybase.network.server.WaterAmountPayload;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.NeoForgeMod;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import net.neoforged.neoforge.network.PacketDistributor;

public class FluidTankBlockEntity extends BlockEntity {
	public static final int CAPACITY = 8000;
	private int oldWaterAmount;
	private int waterAmount;
	private boolean subscribed = false;
	private final PipeConnectedHandler handler = new PipeConnectedHandler(this);
	private final FluidTank tank = new FluidTank(CAPACITY, fluidStack -> fluidStack.is(NeoForgeMod.WATER_TYPE.value())) {
		@Override
		protected void onContentsChanged() {
			if (level != null && !level.isClientSide) {
				setChanged();
				PacketDistributor.sendToAllPlayers(new WaterAmountPayload(worldPosition, tank.getFluidAmount()));
				for (Direction direction : Direction.values()) {
					if (direction == Direction.UP) {
						handler.setPressure(direction, 0.0D);
					} else {
						handler.setPressure(direction, (double) this.getFluidAmount() / this.getCapacity());
					}
				}
			}
		}
	};

	public FluidTankBlockEntity(BlockPos pos, BlockState blockState) {
		super(BlockEntityTypeList.FLUID_TANK.get(), pos, blockState);
	}

	public static void clientTick(Level level, BlockPos pos, BlockState state, FluidTankBlockEntity blockEntity) {
		blockEntity.oldWaterAmount = blockEntity.waterAmount;
	}

		@Override
	public void onLoad() {
		super.onLoad();
		this.handler.registerHandler(new StorageInterface(this.tank::getCapacity, this.tank::getFluidAmount, this.tank::fill, this.tank::drain));
	}

	public FluidTank getTank(Direction direction) {
		return this.tank;
	}

	@Override
	protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
		super.loadAdditional(tag, registries);
		this.tank.readFromNBT(registries, tag);
	}

	@Override
	protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
		super.saveAdditional(tag, registries);
		this.tank.writeToNBT(registries, tag);
	}

	public int getFluidAmount() {
		return this.tank.getFluidAmount();
	}

	public int getWaterAmount() {
		return this.waterAmount;
	}

	public int getOldWaterAmount() {
		return this.oldWaterAmount;
	}

	public void setClientWaterAmount(int waterAmount) {
		this.oldWaterAmount = this.waterAmount;
		this.waterAmount = waterAmount;
	}

	public boolean isSubscribed() {
		return this.subscribed;
	}

	public void setSubscribed() {
		this.subscribed = true;
	}

	@Override
	public void setRemoved() {
		this.handler.removeHandler();
		super.setRemoved();
	}
}
