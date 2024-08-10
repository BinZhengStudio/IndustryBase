package net.industrybase.api.pipe.unit;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayDeque;
import java.util.function.BiConsumer;

public class FluidStorage implements IPipeUnit {
	protected final int capacity;
	protected final BlockPos core;
	protected final AABB aabb; // TODO
	protected final IPipeUnit[] neighbors = new IPipeUnit[6];
	protected final double[] pressure = new double[6];
	protected int amount;

	public FluidStorage(BlockPos pos, int capacity) {
		this.core = pos.immutable();
		this.capacity = capacity;
		this.aabb = new AABB(pos);
	}

	@Override
	public int size() {
		return 1;
	}

	@Override
	public int getMaxTick() {
		return 0;
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

		if (!simulate) this.amount += amount;
		return amount;
	}

	@Override
	public int applySpeed(Direction direction, double speed, boolean simulate) {
		return this.addAmount(direction, (int) (speed * 20), simulate);
	}

	@Override
	public double getTick(Direction direction) {
		return 0;
	}

	@Override
	public void addTick(ArrayDeque<Runnable> tasks, ArrayDeque<Runnable> next, Direction direction, double tick) {
	}

	@Override
	public AABB getAABB() {
		return this.aabb;
	}

	@Override
	public int getCapacity() {
		return this.capacity;
	}

	@Override
	public boolean addPipe(BlockPos pos) {
		throw new UnsupportedOperationException();
	}

	@Nullable
	@Override
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

	@Nullable
	@Override
	public IPipeUnit getNeighbor(Direction direction) {
		return this.neighbors[direction.ordinal()];
	}

	@Override
	public IPipeUnit setNeighbor(Direction direction, @Nullable IPipeUnit neighbor) {
		int index = direction.ordinal();
		IPipeUnit old = this.neighbors[index];
		this.neighbors[index] = neighbor;
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
		return false;
	}

	@Override
	public boolean isSingle() {
		return true;
	}

	@Override
	public boolean isStorage() {
		return true;
	}
}
