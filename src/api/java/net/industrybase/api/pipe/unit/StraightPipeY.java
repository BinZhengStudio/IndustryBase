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
				double diff = this.getMaxTick() - this.positiveTick;
				if (tick > diff) tick = diff;
				this.positiveTick += tick;
			} else {
				this.setPressure(next, tasks, this.negativeDirection, (double) (this.size() * this.bottomAmount) / this.getCapacity());
			}
			if (this.fullTick()) {
				double positiveNeighbor = 0.0D;
				if (this.positive != null) positiveNeighbor = this.positive.getPressure(this.negativeDirection);
				this.setPressure(next, tasks, this.negativeDirection, positiveNeighbor + (double) (this.size() * this.bottomAmount) / this.getCapacity());
				this.positiveTick = this.getMaxTick() - this.negativeTick;
			}
			if (this.full()) {
				double negativeNeighbor = 0.0D;
				if (this.negative != null) negativeNeighbor = this.negative.getPressure(this.positiveDirection);
				this.setPressure(next, tasks, this.positiveDirection, negativeNeighbor - (double) (this.size() * this.amount) / this.getCapacity());
				this.positiveTick = 0.0D;
				this.negativeTick = this.getMaxTick();
			}
		}
	}

	@Override
	public int addAmount(Direction direction, int amount, boolean simulate) {
		int result = super.addAmount(direction, amount, simulate);
		if (!simulate) {
			if (direction == Direction.DOWN || this.fullTick()) {
				this.bottomAmount += result;
				this.negativeTick = (double) (this.getMaxTick() * this.bottomAmount) / this.getCapacity();
			}
		}
		return result;
	}
}
