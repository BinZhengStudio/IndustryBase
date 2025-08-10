package net.industrybase.api.pipe.unit;

import net.industrybase.api.pipe.MergeCheckResult;
import net.industrybase.api.pipe.PipeNetwork;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayDeque;
import java.util.Iterator;
import java.util.function.BiConsumer;

public class PipeRouter extends PipeUnit {
	private final PipeUnit[] neighbors = new PipeUnit[6];
	private final double[] pressure = new double[6];
	protected final double[] neighborPressures = new double[6];
	private final double[] ticks = new double[6];
	private final Runnable[] tasks = new Runnable[6];
	private double totalTick;
	private int amount;
	private int nonUpAmount;
	private int horizontalNeighborSize;

	public PipeRouter(PipeNetwork network, BlockPos core) {
		super(network, core,
				new AABB(0.3125D, 0.3125D, 0.3125D, 0.6875D, 0.6875D, 0.6875D));
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
	public void setPressure(ArrayDeque<PipeUnit> tasks, Direction direction, double newPressure) {
		int index = direction.ordinal();
		this.tasks[index] = () -> {
			double pressure = Math.max(newPressure, 0.0D);
			this.pressure[index] = pressure;
			PipeUnit neighbor = this.neighbors[index];
			if (neighbor != null)
				neighbor.onNeighborUpdatePressure(this, direction.getOpposite(), pressure);
		};

		if (!this.submittedTask()) {
			tasks.addLast(this);
			this.setSubmittedTask();
		}
	}

	@Override
	public void onNeighborUpdatePressure(PipeUnit neighbor, Direction direction, double neighborPressure) {
		this.neighborPressures[direction.ordinal()] = neighborPressure;
		super.onNeighborUpdatePressure(neighbor, direction, neighborPressure);
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
	public double getTick(Direction direction) {
		return this.ticks[direction.ordinal()];
	}

	@Override
	protected void setTick(Direction direction, double tick) {
		this.ticks[direction.ordinal()] = Math.clamp(tick, 0.0D, this.getMaxTick());
	}

	@Override
	public void addTick(Direction direction, double tick) { // TODO reset tick
		if (tick > 0.0D) {
			int index = direction.ordinal();
			double diff = this.getMaxTick() - this.ticks[index];
			if (tick > diff) tick = diff;
			this.ticks[index] += tick;
			this.totalTick += tick;

			if (this.fullTick() || this.full()) { // TODO
				double[] shrinkPressure;
				double[] finalPressure = new double[6];
				int neighborSize = this.horizontalNeighborSize;

				double total = 0.0D;
				double nonDownTotal = 0.0D;
				for (Direction value : DIRECTIONS) {
					double pressure = this.neighborPressures[value.ordinal()];

					total += pressure;
					if (value != Direction.DOWN) nonDownTotal += pressure;
				}

				if (this.neighbors[Direction.DOWN.ordinal()] == null) {
					shrinkPressure = this.neighborPressures;
				} else {
					neighborSize += 1; // add bottom side
					shrinkPressure = new double[6];

					if (nonDownTotal <= 0.75D) { // TODO 0.75
						finalPressure[Direction.DOWN.ordinal()] = total;

						// total will contain the pressure rebounded by bottom pipe
						total = this.neighborPressures[Direction.DOWN.ordinal()];

						// keep shrinkPressure zero
					} else {
						finalPressure[Direction.DOWN.ordinal()] = 0.75D;
						total -= 0.75D;
						nonDownTotal -= 0.75D;

						for (int i = 0; i < 6; i++) {
							shrinkPressure[i] = nonDownTotal * (this.neighborPressures[i] / (nonDownTotal + 0.75D));
						}
					}
					// down pressure will not be used to satisfy down requirement
					shrinkPressure[Direction.DOWN.ordinal()] = this.neighborPressures[Direction.DOWN.ordinal()];
				}

				boolean top = false;
				if (this.neighbors[Direction.UP.ordinal()] != null) {
					if ((total / (neighborSize + 1)) > 0.75D) {
						// if average pressure bigger than requirement to push liquid to top side
						// calc top pressure will be meaningful
						neighborSize += 1; // add top side
						top = true;
					}
				}

				for (Direction value : DIRECTIONS) {
					double pressure = shrinkPressure[value.ordinal()] / (neighborSize - 1); // shrink itself

					for (Direction value1 : DIRECTIONS) {
						if (value1 == value) continue;
						if (value1 == Direction.UP && !top) continue;
						if (this.neighbors[value1.ordinal()] == null) continue;

						finalPressure[value1.ordinal()] += pressure;
					}
				}

				for (Direction value : DIRECTIONS) {
					this.setPressure(this.network.getTask(), value, finalPressure[value.ordinal()]);
				}
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
	public int getCapacity() {
		return 200;
	}

	@Override
	public boolean addPipe(BlockPos pos) {
		throw new UnsupportedOperationException();
	}

	public PipeUnit toStraightPipe() {
		Direction direction = null;
		boolean flag = false; // TODO is flag necessary?
		for (Direction value : DIRECTIONS) { // check all neighbors
			if (this.neighbors[value.ordinal()] != null) {
				if (!flag) {
					direction = value;
					flag = true;
				} else if (value.getAxis() != direction.getAxis()) { // if it has neighbor in different axis
					return EmptyUnit.INSTANCE;
				}
			}
		}
		if (direction != null) {
			StraightPipe pipe = StraightPipe.newInstance(this.core, this.network, direction.getAxis(), this.aabb);

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
		if (neighbor == null) {
			this.neighborPressures[index] = 0.0D;
		} else {
			this.neighborPressures[index] = neighbor.getPressure(direction.getOpposite());
		}
		return old;
	}

	@Override
	public void forEachNeighbor(BiConsumer<? super Direction, ? super PipeUnit> action) {
		for (Direction direction : DIRECTIONS) {
			PipeUnit unit = this.neighbors[direction.ordinal()];
			if (unit != null) action.accept(direction, unit);
		}
	}

	@Override
	public void tickTasks() {
		for (int i = 0; i < this.tasks.length; i++) {
			if (this.tasks[i] != null) {
				Runnable task = this.tasks[i];
				// tasks[i] will be assigned again while run() (such as FluidTank#onContentsChanged)
				// must clear before run()
				this.tasks[i] = null;
				task.run();
			}
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
	public MergeCheckResult canMergeWith(Direction direction, AABB neighborAABB) {
		for (Direction side : DIRECTIONS) {
			if (this.neighbors[side.ordinal()] != null && side.getAxis() != direction.getAxis()) {
				return MergeCheckResult.FAIL_DIRECTION;
			}
		}
		if (!this.aabb.equals(neighborAABB)) return MergeCheckResult.FAIL_AABB;
		return MergeCheckResult.PASS;
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
