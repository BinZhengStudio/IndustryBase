package net.industrybase.api.pipe.unit;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayDeque;
import java.util.Iterator;
import java.util.function.BiConsumer;

public class StraightPipe extends PipeUnit {
	protected final Direction.Axis axis;
	protected final AABB aabb; // TODO
	protected final Direction positiveDirection;
	protected final Direction negativeDirection;
	protected int start;
	protected int end;
	private double positivePressure;
	private double negativePressure;
	protected double positiveTick;
	protected double negativeTick;
	private Runnable positiveTask;
	private Runnable negativeTask;
	protected int amount;
	@Nullable
	protected PipeUnit positive;
	@Nullable
	protected PipeUnit negative;

	protected StraightPipe(BlockPos pos, Direction.Axis axis) {
		this(pos, pos.get(axis), pos.get(axis), axis);
	}

	protected StraightPipe(BlockPos core, int start, int end, Direction.Axis axis) {
		super(core);
		this.axis = axis;
		this.aabb = new AABB(core.getX() + 0.3125D, core.getY() + 0.3125D, core.getZ() + 0.3125D,
				core.getX() + 0.6875D, core.getY() + 0.6875D, core.getZ() + 0.6875D);
		this.positiveDirection = Direction.fromAxisAndDirection(axis, Direction.AxisDirection.POSITIVE);
		this.negativeDirection = Direction.fromAxisAndDirection(axis, Direction.AxisDirection.NEGATIVE);
		if (start <= end) {
			this.start = start;
			this.end = end;
		} else {
			this.start = end;
			this.end = start;
		}
	}

	public static StraightPipe newInstance(BlockPos pos, Direction.Axis axis) {
		if (axis == Direction.Axis.Y) return new StraightPipeY(pos);
		return new StraightPipe(pos, axis);
	}

	@Override
	public int size() {
		return this.end - this.start + 1;
	}

	@Override
	public int getMaxTick() {
		return this.size() * 10;
	}

	@Override
	public double getPressure(Direction direction) {
		if (direction == this.positiveDirection) {
			return this.positivePressure;
		} else if (direction == this.negativeDirection) {
			return this.negativePressure;
		}
		return 0.0D;
	}

	@Override
	public void setPressure(ArrayDeque<PipeUnit> tasks, ArrayDeque<PipeUnit> next, Direction direction, double newPressure) {
		if (direction == this.positiveDirection) {
			double pressure = Math.max(newPressure, 0.0D);
			this.positiveTask = () -> {
				this.positivePressure = pressure;
				if (this.positive != null) { // if positive side is closed
					this.positive.onNeighborUpdatePressure(tasks, next, this, this.negativeDirection, pressure);
				} else {
					this.negativePressure = pressure; // rebound pressure
					if (this.negative != null)
						this.negative.onNeighborUpdatePressure(tasks, next, this, this.positiveDirection, pressure);
				}
			};
			tasks.addLast(this);
		} else if (direction == this.negativeDirection) {
			double pressure = Math.max(newPressure, 0.0D);
			this.negativeTask = () -> {
				this.negativePressure = pressure;
				if (this.negative != null) {
					this.negative.onNeighborUpdatePressure(tasks, next, this, this.positiveDirection, pressure);
				} else {
					this.positivePressure = pressure;
					if (this.positive != null)
						this.positive.onNeighborUpdatePressure(tasks, next, this, this.negativeDirection, pressure);
				}
			};
			tasks.addLast(this);
		}
	}

	@Override
	public int getAmount() {
		return this.amount;
	}

	public int addAmount(Direction direction, int amount, boolean simulate) {
		int diff = this.getCapacity() - this.amount;

		// check if amount over the range
		if (amount > diff) {
			amount = diff;
		} else if (amount < 0 && -amount > this.amount) {
			amount = -this.amount;
		}

		if (!simulate) this.amount += amount;
		return amount;
	}

	@Override
	public int applySpeed(Direction direction, double speed, boolean simulate) {
		return this.addAmount(direction, (int) (speed * 20), simulate);
	}

	@Override
	public double getTick(Direction direction) {
		if (direction.getAxis() == this.axis) {
			return direction == this.positiveDirection ? this.positiveTick : this.negativeTick;
		}
		return 0.0D;
	}

	@Override
	public void addTick(ArrayDeque<PipeUnit> tasks, ArrayDeque<PipeUnit> next, Direction direction, double tick) {
		if (tick > 0.0D) {
			if (direction == this.positiveDirection) {
				double diff = this.getMaxTick() - this.positiveTick;
				if (tick > diff) tick = diff;
				this.positiveTick += tick;
			} else if (direction == this.negativeDirection) {
				double diff = this.getMaxTick() - this.negativeTick;
				if (tick > diff) tick = diff;
				this.negativeTick += tick;
			}
			if (this.fullTick() || this.full()) {
				double positiveNeighbor = 0.0D;
				double negativeNeighbor = 0.0D;
				if (this.positive != null) positiveNeighbor = this.positive.getPressure(this.negativeDirection);
				if (this.negative != null) negativeNeighbor = this.negative.getPressure(this.positiveDirection);
				double pressure;
				if (positiveNeighbor > negativeNeighbor) {
					if (this.negativeTick <= 0.0D) {
						pressure = positiveNeighbor;
						this.setPressure(next, tasks, this.negativeDirection, pressure);
					} else {
						pressure = negativeNeighbor;
						this.negativeTick = 0.0D; // reset tick
						this.setPressure(next, tasks, this.positiveDirection, pressure);
						this.setPressure(next, tasks, this.negativeDirection, pressure);
					}
				} else {
					if (this.positiveTick <= 0.0D) {
						pressure = negativeNeighbor;
						this.setPressure(next, tasks, this.positiveDirection, pressure);
					} else {
						pressure = positiveNeighbor;
						this.positiveTick = 0.0D;
						this.setPressure(next, tasks, this.positiveDirection, pressure);
						this.setPressure(next, tasks, this.negativeDirection, pressure);
					}
				}
			}
		} else {
			// TODO
		}
	}

	protected boolean fullTick() {
		return this.positiveTick + this.negativeTick >= this.getMaxTick();
	}

	protected boolean full() {
		return this.amount >= this.getCapacity();
	}

	@Override
	public AABB getAABB() {
		return this.aabb;
	}

	@Override
	public int getCapacity() {
		return this.size() * 200;
	}

	@Override
	public boolean addPipe(BlockPos pos) {
		int axis = pos.get(this.axis);
		if (axis < this.start) {
			this.start = axis;
			return true;
		} else if (axis > this.end) {
			this.end = axis;
			return true;
		}
		return false;
	}

	public PipeUnit merge(Direction direction, PipeUnit neighbor) {
		if (neighbor.getType() == UnitType.STRAIGHT_PIPE) {
			StraightPipe unit = (StraightPipe) neighbor;
			if (direction.getAxisDirection() == Direction.AxisDirection.POSITIVE) {
				this.end = unit.end;
			} else {
				this.start = unit.start;
			}
			return unit;
		} else {
			this.addPipe(neighbor.getCore());
			return neighbor;
		}
	}

	/**
	 * cut unit and convert pos to router
	 *
	 * @param pos the pos want to cut and discard
	 * @return unit of block that need to be update in components map
	 */
	public PipeUnit[] toRouter(BlockPos pos) {
		int axisPos = pos.get(this.axis);
		if (axisPos == this.start) {
			this.start++;

			PipeRouter router = new PipeRouter(this.getPos(axisPos));
			router.setNeighbor(this.negativeDirection, this.negative);
			router.setNeighbor(this.positiveDirection, this);
			if (this.negative != null) this.negative.setNeighbor(this.positiveDirection, router);
			this.negative = router;

			return new PipeUnit[]{router};
		} else if (axisPos == this.end) {
			this.end--;

			PipeRouter router = new PipeRouter(this.getPos(axisPos));
			router.setNeighbor(this.positiveDirection, this.positive);
			router.setNeighbor(this.negativeDirection, this);
			if (this.positive != null) this.positive.setNeighbor(this.negativeDirection, router);
			this.positive = router;

			return new PipeUnit[]{router};
		} else if (axisPos > this.start && axisPos < this.end) {
			PipeRouter router = new PipeRouter(this.getPos(axisPos));
			StraightPipe unit = new StraightPipe(this.core, this.start, axisPos - 1, this.axis);
			this.start = axisPos + 1;

			if (this.negative != null) this.negative.setNeighbor(this.positiveDirection, unit);
			unit.negative = this.negative;

			router.setNeighbor(this.negativeDirection, unit);
			router.setNeighbor(this.positiveDirection, this);

			this.negative = router;
			return new PipeUnit[]{router, unit};
		}
		return EmptyUnit.INSTANCES;
	}

	private BlockPos getPos(int axisPos) {
		return switch (this.axis) {
			case X -> new BlockPos(axisPos, this.core.getY(), this.core.getZ());
			case Y -> new BlockPos(this.core.getX(), axisPos, this.core.getZ());
			case Z -> new BlockPos(this.core.getX(), this.core.getY(), axisPos);
		};
	}

	@Override
	public PipeUnit spilt(BlockPos pos, Direction direction) {
		int axis = pos.get(this.axis);
		if (axis == this.start && direction == this.negativeDirection) {
			if (this.negative != null) this.negative.setNeighbor(this.positiveDirection, null);
			this.negative = null;
		} else if (axis == this.end && direction == this.positiveDirection) {
			if (this.positive != null) this.positive.setNeighbor(this.negativeDirection, null);
			this.positive = null;
		} else if (axis >= this.start && axis <= this.end) {
			StraightPipe unit;
			if (direction == this.positiveDirection) {
				unit = new StraightPipe(pos.relative(direction), axis + 1, this.end, this.axis);
				if (this.positive != null) {
					this.positive.setNeighbor(this.negativeDirection, unit);
					unit.positive = this.positive;
					this.positive = null;
				}
				this.end = axis;
			} else {
				unit = new StraightPipe(pos.relative(direction), this.start, axis - 1, this.axis);
				if (this.negative != null) {
					this.negative.setNeighbor(this.positiveDirection, unit);
					unit.negative = this.negative;
					this.negative = null;
				}
				this.start = axis;
			}
			return unit;
		}
		return EmptyUnit.INSTANCE;
	}

	@Nonnull
	@Override
	public Direction.Axis getAxis() {
		return this.axis;
	}

	public int getNeighborSize() {
		int i = 0;
		if (this.positive != null) i++;
		if (this.negative != null) i++;
		return i;
	}

	@Override
	public PipeUnit getNeighbor(Direction direction) {
		if (direction.getAxis() == this.axis) {
			return direction == this.positiveDirection ? this.positive : this.negative;
		}
		return null;
	}

	public PipeUnit setNeighbor(Direction direction, @Nullable PipeUnit neighbor) {
		PipeUnit old = null;
		if (direction == this.positiveDirection) {
			old = this.positive;
			this.positive = neighbor;
		} else if (direction == this.negativeDirection) {
			old = this.negative;
			this.negative = neighbor;
		}
		return old;
	}

	@Override
	public void forEachNeighbor(BiConsumer<? super Direction, ? super PipeUnit> action) {
		if (this.positive != null) action.accept(this.positiveDirection, this.positive);
		if (this.negative != null) action.accept(this.negativeDirection, this.negative);
	}

	@Override
	public void tickTasks() {
		if (this.positiveTask != null) {
			Runnable task = this.positiveTask;
			// task will be assigned again while run() (such as FluidTank#onContentsChanged)
			// must clear before run()
			this.positiveTask = null;
			task.run();
		}
		if (this.negativeTask != null) {
			Runnable task = this.negativeTask;
			this.negativeTask = null;
			task.run();
		}
	}

	@Override
	public UnitType getType() {
		return UnitType.STRAIGHT_PIPE;
	}

	@Override
	public boolean isSingle() {
		return this.start == this.end;
	}

	/**
	 * Check self's (not neighbor's) status can merge or not
	 *
	 * @param direction the direction
	 * @return can merge or not
	 */
	@Override
	public boolean canMergeWith(Direction direction) {
		if (direction.getAxis() == this.axis) {
			return true;
		} else if (this.isSingle()) {
			return this.negative == null && this.positive == null;
		}
		return false;
	}

	public boolean canMergeWith2(Direction direction, @Nullable PipeUnit unit) {
		if (unit != null && this.axis == unit.getAxis()) {
			return direction.getAxis() == this.axis;
		} else if (this.isSingle()) {
			return this.negative == null && this.positive == null;
		}
		return false;
	}

	@NotNull
	@Override
	public Iterator<BlockPos> iterator() {
		return new PipeUnitIterator(this.start, this.end, this.axis, this.core);
	}

	private static class PipeUnitIterator implements Iterator<BlockPos> {
		public int point;
		public int end;
		public Direction.Axis axis;
		public BlockPos pos;

		private PipeUnitIterator(int start, int end, Direction.Axis axis, BlockPos pos) {
			this.point = start;
			this.end = end;
			this.axis = axis;
			this.pos = pos;
		}

		@Override
		public boolean hasNext() {
			return this.point <= this.end;
		}

		@Override
		public BlockPos next() {
			BlockPos pos = this.pos;
			switch (this.axis) {
				case X -> pos = new BlockPos(this.point, this.pos.getY(), this.pos.getZ());
				case Y -> pos = new BlockPos(this.pos.getX(), this.point, this.pos.getZ());
				case Z -> pos = new BlockPos(this.pos.getX(), this.pos.getY(), this.point);
			}
			this.point++;
			return pos;
		}
	}
}
