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

public class StraightPipe implements IPipeUnit {
	protected final Direction.Axis axis;
	protected final BlockPos core;
	protected final AABB aabb; // TODO
	protected final Direction positiveDirection;
	protected final Direction negativeDirection;
	protected int start;
	protected int end;
	private double positivePressure;
	private double negativePressure;
	protected double positiveTick;
	protected double negativeTick;
	protected int amount;
	@Nullable
	protected IPipeUnit positive;
	@Nullable
	protected IPipeUnit negative;

	protected StraightPipe(BlockPos pos, Direction.Axis axis) {
		this(pos, pos.get(axis), pos.get(axis), axis);
	}

	protected StraightPipe(BlockPos pos, int start, int end, Direction.Axis axis) {
		this.core = pos.immutable();
		this.axis = axis;
		this.aabb = new AABB(pos.getX() + 0.3125D, pos.getY() + 0.3125D, pos.getZ() + 0.3125D,
				pos.getX() + 0.6875D, pos.getY() + 0.6875D, pos.getZ() + 0.6875D);
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
	public boolean setPressure(ArrayDeque<Runnable> tasks, ArrayDeque<Runnable> next, Direction direction, double pressure) {
		if (pressure < 0.0D) pressure = 0.0D;
		if (direction == this.positiveDirection) {
			this.positivePressure = pressure;
			if (this.positive != null)
				this.positive.onNeighborUpdatePressure(tasks, next, this, this.negativeDirection, pressure);
			return true;
		} else if (direction == this.negativeDirection) {
			this.negativePressure = pressure;
			if (this.negative != null)
				this.negative.onNeighborUpdatePressure(tasks, next, this, this.positiveDirection, pressure);
			return true;
		}
		return false;
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
	public void addTick(ArrayDeque<Runnable> tasks, ArrayDeque<Runnable> next, Direction direction, double tick) {
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
					pressure = (this.negativeTick <= 0.0D ? positiveNeighbor : negativeNeighbor);
					this.negativeTick = 0.0D; // reset tick
				} else {
					pressure = (this.positiveTick <= 0.0D ? negativeNeighbor : positiveNeighbor);
					this.positiveTick = 0.0D;
				}
				this.setPressure(next, tasks, this.positiveDirection, pressure);
				this.setPressure(next, tasks, this.negativeDirection, pressure);
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

	public IPipeUnit merge(Direction direction, IPipeUnit neighbor) {
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
	public IPipeUnit[] toRouter(BlockPos pos) {
		int axisPos = pos.get(this.axis);
		if (axisPos == this.start) {
			this.start++;

			PipeRouter router = new PipeRouter(this.getPos(axisPos));
			router.setNeighbor(this.negativeDirection, this.negative);
			router.setNeighbor(this.positiveDirection, this);
			if (this.negative != null) this.negative.setNeighbor(this.positiveDirection, router);
			this.negative = router;

			return new IPipeUnit[]{router};
		} else if (axisPos == this.end) {
			this.end--;

			PipeRouter router = new PipeRouter(this.getPos(axisPos));
			router.setNeighbor(this.positiveDirection, this.positive);
			router.setNeighbor(this.negativeDirection, this);
			if (this.positive != null) this.positive.setNeighbor(this.negativeDirection, router);
			this.positive = router;

			return new IPipeUnit[]{router};
		} else if (axisPos > this.start && axisPos < this.end) {
			PipeRouter router = new PipeRouter(this.getPos(axisPos));
			StraightPipe unit = new StraightPipe(this.core, this.start, axisPos - 1, this.axis);
			this.start = axisPos + 1;

			if (this.negative != null) this.negative.setNeighbor(this.positiveDirection, unit);
			unit.negative = this.negative;

			router.setNeighbor(this.negativeDirection, unit);
			router.setNeighbor(this.positiveDirection, this);

			this.negative = router;
			return new IPipeUnit[]{router, unit};
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
	public IPipeUnit spilt(BlockPos pos, Direction direction) {
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

	@Override
	public BlockPos getCore() {
		return this.core;
	}

	public int getNeighborSize() {
		int i = 0;
		if (this.positive != null) i++;
		if (this.negative != null) i++;
		return i;
	}

	@Override
	public IPipeUnit getNeighbor(Direction direction) {
		if (direction.getAxis() == this.axis) {
			return direction == this.positiveDirection ? this.positive : this.negative;
		}
		return null;
	}

	public IPipeUnit setNeighbor(Direction direction, @Nullable IPipeUnit neighbor) {
		IPipeUnit old = null;
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
	public void forEachNeighbor(BiConsumer<? super Direction, ? super IPipeUnit> action) {
		if (this.positive != null)
			action.accept(this.positiveDirection, this.positive);
		if (this.negative != null)
			action.accept(this.negativeDirection, this.negative);
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

	public boolean canMergeWith2(Direction direction, @Nullable IPipeUnit unit) {
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
