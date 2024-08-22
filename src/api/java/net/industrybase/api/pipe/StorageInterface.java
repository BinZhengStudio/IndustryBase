package net.industrybase.api.pipe;

import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;

import java.util.function.IntSupplier;

public class StorageInterface {
	private final IntSupplier getCapacity;
	private final IntSupplier getAmount;
	private final AmountConsumer addAmount;

	public StorageInterface(IntSupplier getCapacity,
							IntSupplier getAmount,
							AmountConsumer addAmount) {
		this.getCapacity = getCapacity;
		this.getAmount = getAmount;
		this.addAmount = addAmount;
	}

	public int getCapacity() {
		return this.getCapacity.getAsInt();
	}

	public int getAmount() {
		return this.getAmount.getAsInt();
	}

	public int addAmount(int amount, boolean simulate) {
		return this.addAmount.accept(new FluidStack(Fluids.WATER, amount), simulate ? IFluidHandler.FluidAction.SIMULATE : IFluidHandler.FluidAction.EXECUTE);
	}

	@FunctionalInterface
	public interface AmountConsumer {
		int accept(FluidStack resource, IFluidHandler.FluidAction action);
	}
}
