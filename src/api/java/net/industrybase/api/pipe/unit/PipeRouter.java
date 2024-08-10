package net.industrybase.api.pipe.unit;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.function.BiConsumer;

public class PipeRouter implements IPipeUnit {
	private final BlockPos core;
	private final AABB aabb; // TODO
	private final IPipeUnit[] neighbors = new IPipeUnit[6];
	private final double[] pressure = new double[6];
	private final double[] ticks = new double[6];
	private double totalTick;
	private int amount;
	private int nonUpAmount;
	private int horizontalNeighborSize;

	public PipeRouter(BlockPos pos) {
		this.core = pos.immutable();
		this.aabb = new AABB(pos.getX() + 0.3125D, pos.getY() + 0.3125D, pos.getZ() + 0.3125D,
				pos.getX() + 0.6875D, pos.getY() + 0.6875D, pos.getZ() + 0.6875D);
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
	public boolean setPressure(ArrayDeque<Runnable> tasks, ArrayDeque<Runnable> next, Direction direction, double pressure) {
		int index = direction.ordinal();
		if (pressure < 0.0D) pressure = 0.0D;
		this.pressure[index] = pressure;
		IPipeUnit neighbor = this.neighbors[index];
		if (neighbor != null)
			neighbor.onNeighborUpdatePressure(tasks, next, this, direction.getOpposite(), pressure);
		return true;
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
	public void addTick(ArrayDeque<Runnable> tasks, ArrayDeque<Runnable> next, Direction direction, double tick) {
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
					IPipeUnit neighbor = this.neighbors[value.ordinal()];
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
						IPipeUnit neighbor = this.neighbors[value.ordinal()];
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

	@Nullable
	@Override
	@Deprecated
	public PipeUnit link(IPipeUnit neighbor) {
		if (neighbor.isUnit()) {
			PipeUnit unit = (PipeUnit) neighbor;
			Direction.Axis axis = unit.getAxis();
			int posAxis = this.core.get(axis);
			if (posAxis == unit.start - 1) {
				Direction direction = Direction.fromAxisAndDirection(axis, Direction.AxisDirection.POSITIVE);
				this.setNeighbor(direction, neighbor);
				neighbor.setNeighbor(direction.getOpposite(), this);
			} else if (posAxis == unit.end + 1) {
				Direction direction = Direction.fromAxisAndDirection(axis, Direction.AxisDirection.NEGATIVE);
				this.setNeighbor(direction, neighbor);
				neighbor.setNeighbor(direction.getOpposite(), this);
			}
		} else {
			for (Direction direction : Direction.values()) {
				if (this.core.relative(direction).equals(neighbor.getCore())) {
					this.setNeighbor(direction, neighbor);
					neighbor.setNeighbor(direction.getOpposite(), this);
				}
			}
		}
		return null;
	}

	@Nullable
	@Override
	public PipeUnit cut(BlockPos pos) {
		throw new UnsupportedOperationException();
	}

	@Nullable
	@Override
	public PipeUnit spilt(BlockPos pos, Direction direction) {
		IPipeUnit neighbor = this.neighbors[direction.ordinal()];
		if (neighbor != null) {
			neighbor.setNeighbor(direction.getOpposite(), null);
			this.setNeighbor(direction, null);
		}
		return null;
	}

	@Override
	public Direction.Axis getAxis() {
		throw new UnsupportedOperationException();
	}

	@Override
	public BlockPos getCore() {
		return this.core;
	}

	@Override
	public boolean contains(BlockPos pos) {
		return pos.equals(this.core);
	}

	@Override
	public IPipeUnit getNeighbor(Direction direction) {
		return this.neighbors[direction.ordinal()];
	}

	@Override
	public IPipeUnit setNeighbor(Direction direction, @Nullable IPipeUnit neighbor) {
		int index = direction.ordinal();
		IPipeUnit old = this.neighbors[index];
		this.neighbors[index] = neighbor;
		if (old != neighbor && direction.getAxis().isHorizontal()) {
			if (old == null) this.horizontalNeighborSize++;
			if (neighbor == null) this.horizontalNeighborSize--;
		}
		return old;
	}

	@Override
	public void forEachNeighbor(BiConsumer<? super Direction, ? super IPipeUnit> action) {
		for (Direction direction : Direction.values()) {
			action.accept(direction, this.neighbors[direction.ordinal()]);
		}
	}

	@Override
	public boolean isUnit() {
		return false;
	}

	@Override
	public boolean isRouter() {
		return true;
	}

	@Override
	public boolean isSingle() {
		return true;
	}

	@Override
	public boolean isStorage() {
		return false;
	}
}
