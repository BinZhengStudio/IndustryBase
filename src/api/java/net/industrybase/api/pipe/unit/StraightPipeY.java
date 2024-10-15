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
	public void setPressure(ArrayDeque<PipeUnit> tasks, ArrayDeque<PipeUnit> next, Direction direction, double newPressure) {
		if (direction == Direction.UP) {
			double pressure = Math.max(newPressure, 0.0D);
			this.tasks[0] = () -> {
				this.pressures[0] = pressure;
				if (this.neighbors[0] != null)
					this.neighbors[0].onNeighborUpdatePressure(tasks, next, this, this.directions[1], pressure);
			};
			tasks.addLast(this);
		} else if (direction == Direction.DOWN) {
			double pressure = Math.max(newPressure, 0.0D);
			this.tasks[1] = () -> {
				this.pressures[1] = pressure;
				if (this.neighbors[1] != null)
					this.neighbors[1].onNeighborUpdatePressure(tasks, next, this, this.directions[0], pressure);
			};
			tasks.addLast(this);
		}
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
				this.setPressure(next, tasks, this.directions[1], this.neighborPressures[0] + (double) (this.size() * this.bottomAmount) / this.getCapacity());
				this.ticks[0] = this.getMaxTick() - this.ticks[1];
			}
			if (this.full()) {
				this.setPressure(next, tasks, this.directions[0], this.neighborPressures[1] - (double) (this.size() * this.amount) / this.getCapacity());
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
				if (this.bottomAmount < 0) this.bottomAmount = 0;
				this.ticks[1] = (double) (this.getMaxTick() * this.bottomAmount) / this.getCapacity();
			}
		}
		return result;
	}
}
