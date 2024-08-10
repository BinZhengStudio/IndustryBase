package net.industrybase.api.pipe.unit;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.ArrayDeque;
import java.util.Iterator;
import java.util.function.BiConsumer;

public class PipeUnit implements IPipeUnit, Iterable<BlockPos> {
	protected final Direction.Axis axis;
	protected final BlockPos core;
	protected final AABB aabb; // TODO
	protected final Direction positiveDirection;
	protected final Direction negativeDirection;
	protected int start;
	protected int end;
	protected double positivePressure;
	protected double negativePressure;
	protected double positiveTick;
	protected double negativeTick;
	protected int amount;
	@Nullable
	protected IPipeUnit positive;
	@Nullable
	protected IPipeUnit negative;

	protected PipeUnit(BlockPos pos, Direction.Axis axis) {
		this(pos, pos.get(axis), pos.get(axis), axis);
	}

	protected PipeUnit(BlockPos pos, int start, int end, Direction.Axis axis) {
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

	public static PipeUnit newInstance(BlockPos pos, Direction.Axis axis) {
		if (axis == Direction.Axis.Y) return new PipeUnitY(pos);
		return new PipeUnit(pos, axis);
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
			if (this.fullTick()) {
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

	@Nullable
	@Override
	public PipeUnit link(IPipeUnit neighbor) {
		if (neighbor.isUnit()) {
			PipeUnit unit = (PipeUnit) neighbor;
			if (this.end == unit.start - 1) {
				this.end = unit.end;
			} else if (this.start == unit.end + 1) {
				this.start = unit.start;
			}
			return unit;
		} else {
			int neighborAxis = neighbor.getCore().get(this.axis);
			if (neighborAxis > this.end) {
				this.positive = neighbor;
				neighbor.setNeighbor(this.negativeDirection, this);
			} else if (neighborAxis < this.start) {
				this.negative = neighbor;
				neighbor.setNeighbor(this.positiveDirection, this);
			}
		}
		return null;
	}

	@Nullable
	@Override
	public PipeUnit cut(BlockPos pos) {
		int axis = pos.get(this.axis);
		if (axis == this.start) {
			this.start++;
			this.negative = null;
			return null;
		} else if (axis == this.end) {
			this.end--;
			this.positive = null;
			return null;
		} else if (axis > this.start && axis < this.end) {
			PipeUnit unit = new PipeUnit(this.core, this.start, axis - 1, this.axis);
			this.start = axis + 1;
			if (this.negative != null) {
				this.negative.setNeighbor(Direction.get(Direction.AxisDirection.POSITIVE, this.axis), unit);
				unit.negative = this.negative;
				this.negative = null;
			}
			return unit;
		}
		return null;
	}

	@Nullable
	@Override
	public PipeUnit spilt(BlockPos pos, Direction direction) {
		int axis = pos.get(this.axis);
		if (axis == this.start && direction == this.negativeDirection) {
			if (this.negative != null) this.negative.setNeighbor(direction.getOpposite(), null);
		} else if (axis == this.end && direction == this.positiveDirection) {
			if (this.positive != null) this.positive.setNeighbor(direction.getOpposite(), null);
		} else if (axis >= this.start && axis <= this.end) {
			PipeUnit unit;
			if (direction == this.positiveDirection) {
				unit = new PipeUnit(pos.relative(direction), axis + 1, this.end, this.axis);
				if (this.positive != null) {
					this.positive.setNeighbor(direction.getOpposite(), unit);
					unit.positive = this.positive;
					this.positive = null;
				}
				this.end = axis;
			} else {
				unit = new PipeUnit(pos.relative(direction), this.start, axis - 1, this.axis);
				if (this.negative != null) {
					this.negative.setNeighbor(direction.getOpposite(), unit);
					unit.negative = this.negative;
					this.negative = null;
				}
				this.start = axis;
			}
			return unit;
		}
		return null;
	}

	@Override
	public Direction.Axis getAxis() {
		return this.axis;
	}

	@Override
	public BlockPos getCore() {
		return this.core;
	}

	@Override
	public boolean contains(BlockPos pos) {
		int posAxis = pos.get(this.axis);
		int axisDiff = this.core.get(this.axis) - posAxis;
		return this.start <= posAxis && posAxis <= this.end && pos.distSqr(this.core) == axisDiff * axisDiff;
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
	public boolean isUnit() {
		return true;
	}

	@Override
	public boolean isRouter() {
		return false;
	}

	@Override
	public boolean isSingle() {
		return this.start == this.end;
	}

	@Override
	public boolean isStorage() {
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
