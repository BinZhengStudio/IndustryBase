package net.industrybase.api.pipe;

import net.industrybase.api.CapabilityList;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import org.jetbrains.annotations.NotNull;

public class PipeConnectedHandler implements IFluidHandler {
	public final LazyOptional<IFluidHandler> lazyOptional = LazyOptional.of(() -> this);

	public void register() {
		// TODO
	}

	public void registerPipe() {
		// TODO
	}

	public <X> LazyOptional<X> cast(Capability<X> cap, LazyOptional<X> defaultCap) {
		if (cap == CapabilityList.ELECTRIC_POWER) {
			return this.lazyOptional.cast();
		}
		return defaultCap;
	}

	@Override
	public int getTanks() {
		return 0;
	}

	@Override
	public @NotNull FluidStack getFluidInTank(int tank) {
		return null;
	}

	@Override
	public int getTankCapacity(int tank) {
		return 0;
	}

	@Override
	public boolean isFluidValid(int tank, @NotNull FluidStack stack) {
		return false;
	}

	@Override
	public int fill(FluidStack resource, FluidAction action) {
		return 0;
	}

	@Override
	public @NotNull FluidStack drain(FluidStack resource, FluidAction action) {
		return null;
	}

	@Override
	public @NotNull FluidStack drain(int maxDrain, FluidAction action) {
		return null;
	}
}
