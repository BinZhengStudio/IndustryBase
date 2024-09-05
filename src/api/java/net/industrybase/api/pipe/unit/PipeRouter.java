package net.industrybase.api.pipe.unit;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.function.BiConsumer;

public class PipeRouter extends PipeUnit {
	private final AABB aabb; // TODO
	private final PipeUnit[] neighbors = new PipeUnit[6];
	private final double[] pressure = new double[6];
	private final double[] ticks = new double[6];
	private final Runnable[] tasks = new Runnable[6];
	private double totalTick;
	private int amount;
	private int nonUpAmount;
	private int horizontalNeighborSize;

	public PipeRouter(BlockPos core) {
		super(core);
		this.aabb = new AABB(core.getX() + 0.3125D, core.getY() + 0.3125D, core.getZ() + 0.3125D,
				core.getX() + 0.6875D, core.getY() + 0.6875D, core.getZ() + 0.6875D);
	}

	@Override
	public int size() {
		return 1;
	}

	@Override
	public int getMaxTick() {
		return 10;
	}

	@Override
	public double getPressure(Direction direction) {
		return this.pressure[direction.ordinal()];
	}

	@Override
	public void setPressure(ArrayDeque<PipeUnit> tasks, ArrayDeque<PipeUnit> next, Direction direction, double newPressure) {
		int index = direction.ordinal();
		this.tasks[index] = () -> {
			double pressure = Math.max(newPressure, 0.0D);
			this.pressure[index] = pressure;
			PipeUnit neighbor = this.neighbors[index];
			if (neighbor != null)
				neighbor.onNeighborUpdatePressure(tasks, next, this, direction.getOpposite(), pressure);
		};
		tasks.addLast(this);
	}

	@Override
	public int getAmount() {
		return this.amount;
	}

	@Override
	public int addAmount(Direction direction, int amount, boolean simulate) {
		int diff = this.getCapacity() - this.amount;

		// check if amount over the range
		if (amount > diff) {
			amount = diff;
		} else if (amount < 0 && -amount > this.amount) {
			amount = -this.amount;
		}

		if (!simulate) {
			this.amount += amount;
			if (direction != Direction.UP || this.verticalFullTick()) {
				this.nonUpAmount += amount;
				double bottomTick = (double) (this.getMaxTick() * this.nonUpAmount) / this.getCapacity();
				double tickDiff = bottomTick - this.ticks[Direction.DOWN.ordinal()];
				this.totalTick += tickDiff;
				this.ticks[Direction.DOWN.ordinal()] = bottomTick;
			}
		}
		return amount;
	}

	@Override
	public int applySpeed(Direction direction, double speed, boolean simulate) {
		return this.addAmount(direction, (int) (speed * 20), simulate);
	}

	@Override
	public double getTick(Direction direction) {
		return this.ticks[direction.ordinal()];
	}

	@Override
	public void addTick(ArrayDeque<PipeUnit> tasks, ArrayDeque<PipeUnit> next, Direction direction, double tick) {
		if (tick > 0.0D) {
			int index = direction.ordinal();
			double diff = this.getMaxTick() - this.ticks[index];
			if (tick > diff) tick = diff;
			this.ticks[index] += tick;
			this.totalTick += tick;

			double nonUpPressure = 0.0D;
			if (this.fullTick()) { // TODO
				double minPressure = Double.MAX_VALUE;
				ArrayList<Direction> minDirections = new ArrayList<>(6);
				for (Direction value : Direction.values()) {
					PipeUnit neighbor = this.neighbors[value.ordinal()];
					if (neighbor != null) {
						double pressure = neighbor.getPressure(direction.getOpposite());
						if (pressure < minPressure) {
							minPressure = Math.min(minPressure, pressure);
							minDirections.clear();
							minDirections.add(value);
						} else if (pressure == minPressure) {
							minDirections.add(value);
						}
					}
				}

				minDirections.forEach(value -> this.ticks[value.ordinal()] = 0.0D); // reset ticks

				for (Direction value : Direction.values()) {
					if (value != Direction.UP) {
						this.setPressure(next, tasks, value, minPressure);
						PipeUnit neighbor = this.neighbors[value.ordinal()];
						nonUpPressure += neighbor == null ? 0.0D : neighbor.getPressure(value.getOpposite());
					}
				}
			}
			if (this.full()) {
				this.setPressure(next, tasks, Direction.UP, nonUpPressure - (double) (this.size() * this.amount) / this.getCapacity());
			}
		} else {
			// TODO
		}
	}

	private boolean fullTick() {
		return this.totalTick >= this.getMaxTick();
	}

	private boolean verticalFullTick() {
		return this.ticks[Direction.DOWN.ordinal()] + this.ticks[Direction.UP.ordinal()] >= this.getMaxTick();
	}

	private boolean full() {
		return this.amount >= this.getCapacity();
	}

	@Override
	public AABB getAABB() {
		return this.aabb;
	}

	@Override
	public int getCapacity() {
		return 200;
	}

	@Override
	public boolean addPipe(BlockPos pos) {
		throw new UnsupportedOperationException();
	}

	public PipeUnit toStraightPipe() {
		Direction direction = null;
		boolean flag = false;
		for (Direction value : Direction.values()) {
			if (this.neighbors[value.ordinal()] != null) {
				if (!flag) {
					direction = value;
					flag = true;
				} else if (value.getAxis() != direction.getAxis()) {
					return EmptyUnit.INSTANCE;
				}
			}
		}
		if (direction != null) {
			StraightPipe pipe = StraightPipe.newInstance(this.core, direction.getAxis());

			PipeUnit neighbor = this.neighbors[direction.ordinal()];
			pipe.setNeighbor(direction, neighbor);
			neighbor.setNeighbor(direction.getOpposite(), pipe);

			PipeUnit oppositeNeighbor = this.neighbors[direction.getOpposite().ordinal()];
			if (oppositeNeighbor != null) {
				pipe.setNeighbor(direction.getOpposite(), oppositeNeighbor);
				oppositeNeighbor.setNeighbor(direction, pipe);
			}

			return pipe;
		}
		return EmptyUnit.INSTANCE;
	}

	@Override
	public PipeUnit spilt(BlockPos pos, Direction direction) {
		PipeUnit neighbor = this.neighbors[direction.ordinal()];
		if (neighbor != null) {
			neighbor.setNeighbor(direction.getOpposite(), null);
			this.setNeighbor(direction, null);
		}
		return EmptyUnit.INSTANCE;
	}

	@Override
	public Direction.Axis getAxis() {
		return null;
	}

	@Override
	public PipeUnit getNeighbor(Direction direction) {
		return this.neighbors[direction.ordinal()];
	}

	@Override
	public PipeUnit setNeighbor(Direction direction, @Nullable PipeUnit neighbor) {
		int index = direction.ordinal();
		PipeUnit old = this.neighbors[index];
		this.neighbors[index] = neighbor;
		if (old != neighbor && direction.getAxis().isHorizontal()) {
			if (old == null) this.horizontalNeighborSize++;
			if (neighbor == null) this.horizontalNeighborSize--;
		}
		return old;
	}

	@Override
	public void forEachNeighbor(BiConsumer<? super Direction, ? super PipeUnit> action) {
		for (Direction direction : Direction.values()) {
			PipeUnit unit = this.neighbors[direction.ordinal()];
			if (unit != null) action.accept(direction, unit);
		}
	}

	@Override
	public void tickTasks() {
		for (Runnable task : this.tasks) {
			if (task != null) task.run();
		}
	}

	@Override
	public UnitType getType() {
		return UnitType.ROUTER;
	}

	@Override
	public boolean isSingle() {
		return true;
	}

	@Override
	public boolean canMergeWith(Direction direction) {
		for (Direction side : Direction.values()) {
			if (this.neighbors[side.ordinal()] != null && side.getAxis() != direction.getAxis()) {
				return false;
			}
		}
		return true;
	}

	@NotNull
	@Override
	public Iterator<BlockPos> iterator() {
		return new SingleUnitIterator(this.core);
	}

	protected static class SingleUnitIterator implements Iterator<BlockPos> {
		public boolean iterated;
		public BlockPos core;

		protected SingleUnitIterator(BlockPos core) {
			this.iterated = false;
			this.core = core;
		}

		@Override
		public boolean hasNext() {
			return !this.iterated;
		}

		@Override
		public BlockPos next() {
			this.iterated = true;
			return this.core;
		}
	}
}
