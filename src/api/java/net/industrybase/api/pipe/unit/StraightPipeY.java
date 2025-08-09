package net.industrybase.api.pipe.unit;

import net.industrybase.api.pipe.PipeNetwork;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.AABB;

public class StraightPipeY extends StraightPipe {
	private int bottomAmount;

	protected StraightPipeY(PipeNetwork network, BlockPos pos, AABB aabb) {
		super(network, pos, Direction.Axis.Y, aabb);
	}

	@Override
	public int getMaxTick() {
		return this.size() * 5;
	}

	@Override
	public void addTick(Direction direction, double tick) {
		if (tick > 0.0D) {
			if (direction == Direction.UP) {
				double diff = this.getMaxTick() - this.ticks[0];
				if (tick > diff) tick = diff;
				this.ticks[0] += tick;
			}
			this.ticks[1] = (double) (this.getMaxTick() * this.bottomAmount) / this.getCapacity();

			double amountPressure = (double) (this.size() * this.bottomAmount) / this.getCapacity();

			if (this.fullTick() || this.full()) {
				this.ticks[0] = 0.0D;
				if (this.neighbors[0] != null) {
					this.setPressure(this.network.getTask(), this.directions[1], this.neighborPressures[0] + amountPressure);
				} else { // rebound pressure
					this.setPressure(this.network.getTask(), this.directions[1], Math.max(this.neighborPressures[1], amountPressure));
				}
			} else {
				this.setPressure(this.network.getTask(), this.directions[1], amountPressure);
			}

			if (this.full()) {
				this.ticks[0] = 0.0D;
				this.ticks[1] = this.getMaxTick();
				if (this.neighbors[1] != null) {
					this.setPressure(this.network.getTask(), this.directions[0], this.neighborPressures[1] - amountPressure);
				} else {
					this.setPressure(this.network.getTask(), this.directions[0], this.neighborPressures[0]);
				}
			} else {
				this.setPressure(this.network.getTask(), this.directions[0], 0.0D);
			}
		}
	}

	@Override
	public int addAmount(Direction direction, int amount, boolean simulate) {
		int result = super.addAmount(direction, amount, simulate);
		if (!simulate) {
			if (direction == Direction.DOWN || this.fullTick()) {
				this.bottomAmount += result;
				if (this.bottomAmount < 0) this.bottomAmount = 0;
			}
		}
		return result;
	}
}
