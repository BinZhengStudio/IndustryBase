package net.industrybase.api.pipe;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.Iterator;
import java.util.function.Consumer;

public class PipeUnit implements IPipeUnit, Iterable<BlockPos> {
	private final Direction.Axis axis;
	private BlockPos core;
	private int start;
	private int end;
	@Nullable
	private IPipeUnit positive;
	@Nullable
	private IPipeUnit negative;

	public PipeUnit(BlockPos pos, Direction.Axis axis) {
		this(pos, pos.get(axis), pos.get(axis), axis);
	}

	public PipeUnit(BlockPos pos, int start, int end, Direction.Axis axis) {
		this.core = pos.immutable();
		this.axis = axis;
		if (start <= end) {
			this.start = start;
			this.end = end;
		} else {
			this.start = end;
			this.end = start;
		}
	}

	@Override
	public int size() {
		return this.end - this.start + 1;
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
		if (!neighbor.isRouter()) {
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
			} else if (neighborAxis < this.start) {
				this.negative = neighbor;
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
		if (axis == this.start && direction.getAxisDirection() == Direction.AxisDirection.NEGATIVE) {
			if (this.negative != null) this.negative.setNeighbor(direction.getOpposite(), null);
		} else if (axis == this.end && direction.getAxisDirection() == Direction.AxisDirection.POSITIVE) {
			if (this.positive != null) this.positive.setNeighbor(direction.getOpposite(), null);
		} else if (axis >= this.start && axis <= this.end) {
			PipeUnit unit;
			if (direction.getAxisDirection() == Direction.AxisDirection.POSITIVE) {
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

	public int getStart() {
		return this.start;
	}

	public int getEnd() {
		return this.end;
	}

	@Override
	public IPipeUnit getNeighbor(Direction direction) {
		if (direction.getAxis() == this.axis) {
			return direction.getAxisDirection() == Direction.AxisDirection.POSITIVE ? this.positive : this.negative;
		}
		return null;
	}

	public void setNeighbor(Direction direction, @Nullable IPipeUnit neighbor) {
		if (direction.getAxis() == this.axis) {
			if (direction.getAxisDirection() == Direction.AxisDirection.POSITIVE) {
				this.positive = neighbor;
			} else {
				this.negative = neighbor;
			}
		}
	}

	@Override
	public void forEachNeighbor(Consumer<? super IPipeUnit> action) {
		if (this.positive != null) action.accept(this.positive);
		if (this.negative != null) action.accept(this.negative);
	}

	@Override
	public boolean isRouter() {
		return false;
	}

	@Override
	public boolean isSingle() {
		return this.start == this.end;
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
