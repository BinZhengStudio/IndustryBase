package net.industrybase.api.pipe.unit;

import net.industrybase.api.pipe.MergeCheckResult;
import net.industrybase.api.pipe.PipeNetwork;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayDeque;
import java.util.EnumMap;
import java.util.Iterator;
import java.util.function.BiConsumer;

public class StraightPipe extends PipeUnit {
	private static final EnumMap<Direction.Axis, Direction[]> DIRECTIONS = new EnumMap<>(Direction.Axis.class);

	static {
		DIRECTIONS.put(Direction.Axis.X, new Direction[]{Direction.EAST, Direction.WEST});
		DIRECTIONS.put(Direction.Axis.Y, new Direction[]{Direction.UP, Direction.DOWN});
		DIRECTIONS.put(Direction.Axis.Z, new Direction[]{Direction.SOUTH, Direction.NORTH});
	}

	protected final Direction.Axis axis;
	// index 0 is positive, index 1 is negative 
	protected final Direction[] directions;
	protected int start;
	protected int end;
	protected final double[] pressures = new double[2];
	protected final double[] neighborPressures = new double[2];
	protected final double[] ticks = new double[2];
	protected final Runnable[] tasks = new Runnable[2];
	protected int amount;
	protected final PipeUnit[] neighbors = new PipeUnit[2];

	protected StraightPipe(PipeNetwork network, BlockPos pos, Direction.Axis axis, AABB aabb) {
		this(network, pos, pos.get(axis), pos.get(axis), axis, aabb);
	}

	protected StraightPipe(PipeNetwork network, BlockPos core, int start, int end, Direction.Axis axis, AABB aabb) {
		super(network, core, aabb);
		this.axis = axis;
		this.directions = DIRECTIONS.get(axis);
		if (start <= end) {
			this.start = start;
			this.end = end;
		} else {
			this.start = end;
			this.end = start;
		}
	}

	public static StraightPipe newInstance(BlockPos pos, PipeNetwork network, Direction.Axis axis, AABB aabb) {
		if (axis == Direction.Axis.Y) return new StraightPipeY(network, pos, aabb);
		return new StraightPipe(network, pos, axis, aabb);
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
		if (direction == this.directions[0]) {
			return this.pressures[0];
		} else if (direction == this.directions[1]) {
			return this.pressures[1];
		}
		return 0.0D;
	}

	@Override
	public void setPressure(ArrayDeque<PipeUnit> tasks, Direction direction, double newPressure) {
		if (direction.getAxis() != this.axis) return;
		int i = direction.getAxisDirection().ordinal();
		int j = (i == 0 ? 1 : 0);

		this.tasks[i] = () -> {
			double pressure = Math.max(newPressure, 0.0D);
			this.pressures[i] = pressure;
			PipeUnit neighbor = this.neighbors[i];
			if (neighbor != null) {
				neighbor.onNeighborUpdatePressure(this, this.directions[j], pressure);
			}
		};

		if (!this.submittedTask()) {
			tasks.addLast(this);
			this.setSubmittedTask();
		}
	}

	@Override
	public void onNeighborUpdatePressure(PipeUnit neighbor, Direction direction, double neighborPressure) {
		this.neighborPressures[direction.getAxisDirection().ordinal()] = neighborPressure;
		super.onNeighborUpdatePressure(neighbor, direction, neighborPressure);
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
	public double getTick(Direction direction) {
		if (direction.getAxis() == this.axis) {
			return direction == this.directions[0] ? this.ticks[0] : this.ticks[1];
		}
		return 0.0D;
	}

	@Override
	protected void setTick(Direction direction, double tick) {
		if (direction == this.directions[0]) {
			this.ticks[0] = Math.clamp(tick, 0.0D, this.getMaxTick());
		} else if (direction == this.directions[1]) {
			this.ticks[1] = Math.clamp(tick, 0.0D, this.getMaxTick());
		}
	}

	@Override
	public void addTick(Direction direction, double tick) {
		if (tick > 0.0D) {
			if (direction == this.directions[0]) {
				double diff = this.getMaxTick() - this.ticks[0];
				if (tick > diff) tick = diff;
				this.ticks[0] += tick;
			} else if (direction == this.directions[1]) {
				double diff = this.getMaxTick() - this.ticks[1];
				if (tick > diff) tick = diff;
				this.ticks[1] += tick;
			}

			if (this.fullTick() || this.full()) {
				if (this.ticks[0] <= 0.0D || this.ticks[1] <= 0.0D || this.neighborPressures[0] == this.neighborPressures[1]) {
					boolean fromPositive = this.ticks[0] > 0.0D;
					boolean fromNegative = this.ticks[1] > 0.0D;
					this.ticks[0] = 0.0D;
					this.ticks[1] = 0.0D;

					// convey or rebound pressure
					int i0 = (this.neighbors[1] == null) ||
							(this.neighborPressures[1] > this.neighborPressures[0] && fromPositive) ? 0 : 1;
					int i1 = (this.neighbors[0] == null) ||
							(this.neighborPressures[0] > this.neighborPressures[1] && fromNegative) ? 1 : 0;

					this.setPressure(this.network.getTask(), this.directions[0], this.neighborPressures[i0]);
					this.setPressure(this.network.getTask(), this.directions[1], this.neighborPressures[i1]);
				} else {
					int i = this.neighborPressures[0] < this.neighborPressures[1] ? 0 : 1;

					this.ticks[i] = 0.0D;
					this.setPressure(this.network.getTask(), this.directions[0], this.neighborPressures[i]);
					this.setPressure(this.network.getTask(), this.directions[1], this.neighborPressures[i]);
				}
			}
		} else {
			// TODO
		}
	}

	protected boolean fullTick() {
		return this.ticks[0] + this.ticks[1] >= this.getMaxTick();
	}

	protected boolean full() {
		return this.amount >= this.getCapacity();
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
		} else {
			this.addPipe(neighbor.getCore());
		}

		PipeUnit neighborNeighbor = neighbor.getNeighbor(direction);
		if (neighborNeighbor != null) neighborNeighbor.setNeighbor(direction.getOpposite(), this);
		this.setNeighbor(direction, neighborNeighbor);

		return neighbor;
	}

	/**
	 * cut unit and convert pos to router
	 *
	 * @param pos the pos want to cut and discard
	 * @return unit of block that need to be update in components map
	 */
	public PipeUnit[] toRouter(BlockPos pos) {
		int axisPos = pos.get(this.axis);
		if (this.isSingle()) {
			PipeRouter router = new PipeRouter(this.network, this.getPos(axisPos));
			router.setNeighbor(this.directions[0], this.neighbors[0]);
			router.setNeighbor(this.directions[1], this.neighbors[1]);
			if (this.neighbors[1] != null) this.neighbors[1].setNeighbor(this.directions[0], router);
			if (this.neighbors[0] != null) this.neighbors[0].setNeighbor(this.directions[1], router);

			router.setTick(this.directions[0], this.ticks[0]);
			router.setTick(this.directions[1], this.ticks[1]);

			return new PipeUnit[]{router};
		} else {
			if (axisPos == this.start) {
				this.start++;

				PipeRouter router = new PipeRouter(this.network, this.getPos(axisPos));
				router.setNeighbor(this.directions[1], this.neighbors[1]);
				router.setNeighbor(this.directions[0], this);
				if (this.neighbors[1] != null) this.neighbors[1].setNeighbor(this.directions[0], router);
				this.neighbors[1] = router;

				router.setTick(this.directions[0], this.ticks[0] - this.getMaxTick());
				this.setTick(this.directions[0], this.ticks[0]);
				router.setTick(this.directions[1], this.ticks[1]);
				this.setTick(this.directions[1], this.ticks[1] - router.getMaxTick());

				return new PipeUnit[]{router};
			} else if (axisPos == this.end) {
				this.end--;

				PipeRouter router = new PipeRouter(this.network, this.getPos(axisPos));
				router.setNeighbor(this.directions[0], this.neighbors[0]);
				router.setNeighbor(this.directions[1], this);
				if (this.neighbors[0] != null) this.neighbors[0].setNeighbor(this.directions[1], router);
				this.neighbors[0] = router;

				router.setTick(this.directions[0], this.ticks[0]);
				this.setTick(this.directions[0], this.ticks[0] - router.getMaxTick());
				router.setTick(this.directions[1], this.ticks[1] - this.getMaxTick());
				this.setTick(this.directions[1], this.ticks[1]);

				return new PipeUnit[]{router};
			} else if (axisPos > this.start && axisPos < this.end) {
				PipeRouter router = new PipeRouter(this.network, this.getPos(axisPos));
				StraightPipe unit = new StraightPipe(this.network, this.core, this.start, axisPos - 1, this.axis, this.aabb);
				this.start = axisPos + 1;

				if (this.neighbors[1] != null) this.neighbors[1].setNeighbor(this.directions[0], unit);

				unit.neighbors[1] = this.neighbors[1];
				unit.neighbors[0] = router;

				router.setNeighbor(this.directions[1], unit);
				router.setNeighbor(this.directions[0], this);

				this.neighbors[1] = router;

				unit.setTick(this.directions[0], this.ticks[0] - this.getMaxTick() - router.getMaxTick());
				router.setTick(this.directions[0], this.ticks[0] - this.getMaxTick());
				this.setTick(this.directions[0], this.ticks[0]);
				unit.setTick(this.directions[1], this.ticks[1]);
				router.setTick(this.directions[1], this.ticks[1] - unit.getMaxTick());
				this.setTick(this.directions[1], this.ticks[1] - unit.getMaxTick() - router.getMaxTick());

				return new PipeUnit[]{router, unit};
			}
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
		if (axis == this.start && direction == this.directions[1]) {
			if (this.neighbors[1] != null) this.neighbors[1].setNeighbor(this.directions[0], null);
			this.neighbors[1] = null;
		} else if (axis == this.end && direction == this.directions[0]) {
			if (this.neighbors[0] != null) this.neighbors[0].setNeighbor(this.directions[1], null);
			this.neighbors[0] = null;
		} else if (axis >= this.start && axis <= this.end) {
			StraightPipe unit;
			if (direction == this.directions[0]) {
				unit = new StraightPipe(this.network, pos.relative(direction), axis + 1, this.end, this.axis, this.aabb);
				this.end = axis;
			} else {
				unit = new StraightPipe(this.network, pos.relative(direction), axis, this.end, this.axis, this.aabb);
				this.end = axis - 1;
			}

			if (this.neighbors[0] != null) {
				this.neighbors[0].setNeighbor(this.directions[1], unit);
				unit.neighbors[0] = this.neighbors[0];
				this.neighbors[0] = null;

				unit.neighborPressures[0] = this.neighborPressures[0];
				this.neighborPressures[0] = 0.0D;
			}

			unit.setTick(this.directions[0], this.ticks[0]);
			this.setTick(this.directions[0], this.ticks[0] - unit.getMaxTick());
			unit.setTick(this.directions[1], this.ticks[1] - this.getMaxTick());
			this.setTick(this.directions[1], this.ticks[1]);

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
		if (this.neighbors[0] != null) i++;
		if (this.neighbors[1] != null) i++;
		return i;
	}

	@Override
	public PipeUnit getNeighbor(Direction direction) {
		if (direction.getAxis() == this.axis) {
			return direction == this.directions[0] ? this.neighbors[0] : this.neighbors[1];
		}
		return null;
	}

	public PipeUnit setNeighbor(Direction direction, @Nullable PipeUnit neighbor) {
		PipeUnit old = null;
		if (direction == this.directions[0]) {
			old = this.neighbors[0];
			this.neighbors[0] = neighbor;
			if (neighbor == null) {
				this.neighborPressures[0] = 0.0D;
			} else {
				this.neighborPressures[0] = neighbor.getPressure(direction.getOpposite());
			}
		} else if (direction == this.directions[1]) {
			old = this.neighbors[1];
			this.neighbors[1] = neighbor;
			if (neighbor == null) {
				this.neighborPressures[1] = 0.0D;
			} else {
				this.neighborPressures[1] = neighbor.getPressure(direction.getOpposite());
			}
		}
		return old;
	}

	@Override
	public void forEachNeighbor(BiConsumer<? super Direction, ? super PipeUnit> action) {
		if (this.neighbors[0] != null) action.accept(this.directions[0], this.neighbors[0]);
		if (this.neighbors[1] != null) action.accept(this.directions[1], this.neighbors[1]);
	}

	@Override
	public void tickTasks() {
		if (this.tasks[0] != null) {
			Runnable task = this.tasks[0];
			// task will be assigned again while run() (such as FluidTank#onContentsChanged)
			// must clear before run()
			this.tasks[0] = null;
			task.run();
		}
		if (this.tasks[1] != null) {
			Runnable task = this.tasks[1];
			this.tasks[1] = null;
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
	public MergeCheckResult canMergeWith(Direction direction, AABB neighborAABB) {
		boolean aabbEqual = this.aabb.equals(neighborAABB);

		if (direction.getAxis() == this.axis) {
			return aabbEqual ? MergeCheckResult.PASS : MergeCheckResult.FAIL_AABB;
		} else if (this.isSingle() && this.neighbors[1] == null && this.neighbors[0] == null) {
			return aabbEqual ? MergeCheckResult.PASS : MergeCheckResult.FAIL_AABB;
		}
		return MergeCheckResult.FAIL_DIRECTION;
	}

	public boolean canMergeWith2(Direction direction, @Nullable PipeUnit unit) {
		if (unit != null && this.axis == unit.getAxis()) {
			return direction.getAxis() == this.axis;
		} else if (this.isSingle()) {
			return this.neighbors[1] == null && this.neighbors[0] == null;
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
