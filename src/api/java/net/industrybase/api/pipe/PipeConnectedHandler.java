package net.industrybase.api.pipe;

import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;

public class PipeConnectedHandler implements IFluidHandler {

	public void register() {
		// TODO
	}

	public void registerPipe() {
		// TODO
	}

	@Override
	public int getTanks() {
		return 0;
	}

	@Override
	public FluidStack getFluidInTank(int tank) {
		return null;
	}

	@Override
	public int getTankCapacity(int tank) {
		return 0;
	}

	@Override
	public boolean isFluidValid(int tank, FluidStack stack) {
		return false;
	}

	@Override
	public int fill(FluidStack resource, FluidAction action) {
		return 0;
	}

	@Override
	public FluidStack drain(FluidStack resource, FluidAction action) {
		return null;
	}

	@Override
	public FluidStack drain(int maxDrain, FluidAction action) {
		return null;
	}
}
