package net.industrybase.world.level.block.entity;

import net.industrybase.api.pipe.PipeConnectedHandler;
import net.industrybase.api.pipe.StorageInterface;
import net.industrybase.network.server.WaterAmountPayload;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.fluid.FluidStacksResourceHandler;
import net.neoforged.neoforge.transfer.transaction.Transaction;

public class FluidTankBlockEntity extends BlockEntity {
	public static final int CAPACITY = 8000;
	private static final AABB AABB = new AABB(0.0D, 0.0D, 0.0D, 1.0D, 1.0D, 1.0D);
	private static final FluidResource WATER_RESOURCE = FluidResource.of(Fluids.WATER);
    private int oldWaterAmount;
	private int waterAmount;
	private boolean subscribed = false;
	private final PipeConnectedHandler handler = new PipeConnectedHandler(this);
	private final FluidStacksResourceHandler tank = new FluidStacksResourceHandler(1, CAPACITY) {
        
		protected void onContentsChanged(int index, FluidStack previousContents) {
			if (level != null && !level.isClientSide()) {
				setChanged();
				PacketDistributor.sendToAllPlayers(new WaterAmountPayload(worldPosition, this.getAmountAsInt(index)));
				for (Direction direction : Direction.values()) {
					if (direction == Direction.UP) {
						handler.setPressure(direction, 0.0D);
					} else {
						handler.setPressure(direction,
                                (double) this.getAmountAsInt(index) / this.getCapacity(index, WATER_RESOURCE));
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
		this.handler.registerHandler(AABB, new StorageInterface(
                () -> this.tank.getAmountAsInt(0),
                () -> this.tank.getCapacityAsInt(0, WATER_RESOURCE),
                (resource, simulate) -> {
                    try (var tx = Transaction.openRoot()) {
                        int result = this.tank.insert(0, FluidResource.of(resource), resource.getAmount(), tx);
                        if (!simulate) tx.commit();
                        return result;
                    }
                },
                (resource, simulate) -> {
                    try (var rx = Transaction.openRoot()) {
                        int result = this.tank.extract(0, FluidResource.of(resource), resource.getAmount(), rx);
                        if (!simulate) rx.commit();
                        return resource.copyWithAmount(result);
                    }
                }));
	}

	public FluidStacksResourceHandler getTank(Direction direction) {
		return this.tank;
	}

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        input.readChild("Fluid", this.tank);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putChild("Fluid", this.tank);
    }

	public int getFluidAmount() {
		return this.tank.getAmountAsInt(0);
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
