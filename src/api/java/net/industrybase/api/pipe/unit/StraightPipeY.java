package net.industrybase.api.pipe.unit;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

import java.util.ArrayDeque;

public class StraightPipeY extends StraightPipe {
	private int bottomAmount;

	protected StraightPipeY(BlockPos pos) {
		super(pos, Direction.Axis.Y);
	}

	protected StraightPipeY(BlockPos pos, int start, int end) {
		super(pos, start, end, Direction.Axis.Y);
	}

	@Override
	public int getMaxTick() {
		return this.size() * 5;
	}

	@Override
	public void addTick(ArrayDeque<PipeUnit> tasks, ArrayDeque<PipeUnit> next, Direction direction, double tick) {
		if (tick > 0.0D) {
			if (direction == Direction.UP) {
				double diff = this.getMaxTick() - this.ticks[0];
				if (tick > diff) tick = diff;
				this.ticks[0] += tick;
			} else {
				this.setPressure(next, tasks, this.directions[1], (double) (this.size() * this.bottomAmount) / this.getCapacity());
			}
			if (this.fullTick()) {
				double positiveNeighbor = 0.0D;
				if (this.positive != null) positiveNeighbor = this.positive.getPressure(this.directions[1]);
				this.setPressure(next, tasks, this.directions[1], positiveNeighbor + (double) (this.size() * this.bottomAmount) / this.getCapacity());
				this.ticks[0] = this.getMaxTick() - this.ticks[1];
			}
			if (this.full()) {
				double negativeNeighbor = 0.0D;
				if (this.negative != null) negativeNeighbor = this.negative.getPressure(this.directions[0]);
				this.setPressure(next, tasks, this.directions[0], negativeNeighbor - (double) (this.size() * this.amount) / this.getCapacity());
				this.ticks[0] = 0.0D;
				this.ticks[1] = this.getMaxTick();
			}
		}
	}

	@Override
	public int addAmount(Direction direction, int amount, boolean simulate) {
		int result = super.addAmount(direction, amount, simulate);
		if (!simulate) {
			if (direction == Direction.DOWN || this.fullTick()) {
				this.bottomAmount += result;
				this.ticks[1] = (double) (this.getMaxTick() * this.bottomAmount) / this.getCapacity();
			}
		}
		return result;
	}
}
