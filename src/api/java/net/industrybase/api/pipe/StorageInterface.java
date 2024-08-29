package net.industrybase.api.pipe;

import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;

import java.util.function.IntSupplier;

public class StorageInterface {
	private final IntSupplier getCapacity;
	private final IntSupplier getAmount;
	private final FillConsumer fill;
	private final DrainConsumer drain;

	public StorageInterface(IntSupplier getCapacity,
							IntSupplier getAmount,
							FillConsumer fill,
							DrainConsumer drain) {
		this.getCapacity = getCapacity;
		this.getAmount = getAmount;
		this.fill = fill;
		this.drain = drain;
	}

	public int getCapacity() {
		return this.getCapacity.getAsInt();
	}

	public int getAmount() {
		return this.getAmount.getAsInt();
	}

	public int addAmount(int amount, boolean simulate) {
		if (amount > 0) {
			return this.fill.accept(new FluidStack(Fluids.WATER, amount), simulate ? IFluidHandler.FluidAction.SIMULATE : IFluidHandler.FluidAction.EXECUTE);
		} else if (amount < 0) {
			FluidStack stack = this.drain.accept(new FluidStack(Fluids.WATER, -amount), simulate ? IFluidHandler.FluidAction.SIMULATE : IFluidHandler.FluidAction.EXECUTE);
			return -stack.getAmount();
		}
		return 0;
	}

	@FunctionalInterface
	public interface FillConsumer {
		int accept(FluidStack resource, IFluidHandler.FluidAction action);
	}

	@FunctionalInterface
	public interface DrainConsumer {
		FluidStack accept(FluidStack resource, IFluidHandler.FluidAction action);
	}
}
