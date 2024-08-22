package net.industrybase.api.pipe.unit;

import net.industrybase.api.pipe.StorageInterface;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayDeque;
import java.util.Iterator;
import java.util.function.BiConsumer;

public class FluidStorage implements IPipeUnit {
	protected final StorageInterface storageInterface;
	protected final BlockPos core;
	protected final AABB aabb; // TODO
	protected final IPipeUnit[] neighbors = new IPipeUnit[6];
	protected final double[] pressure = new double[6];

	public FluidStorage(BlockPos pos, StorageInterface storageInterface) {
		this.core = pos.immutable();
		this.storageInterface = storageInterface;
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
		return this.storageInterface.getAmount();
	}

	@Override
	public int addAmount(Direction direction, int amount, boolean simulate) {
		return this.storageInterface.addAmount(amount, simulate);
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
		return this.storageInterface.getCapacity();
	}

	@Override
	public boolean addPipe(BlockPos pos) {
		throw new UnsupportedOperationException();
	}

	@Override
	public IPipeUnit spilt(BlockPos pos, Direction direction) {
		IPipeUnit neighbor = this.neighbors[direction.ordinal()];
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
	public BlockPos getCore() {
		return this.core;
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
			IPipeUnit unit = this.neighbors[direction.ordinal()];
			if (unit != null) action.accept(direction, unit);
		}
	}

	@Override
	public UnitType getType() {
		return UnitType.FLUID_STORAGE;
	}

	@Override
	public boolean isSingle() {
		return true;
	}

	@Override
	public boolean canMergeWith(Direction direction) {
		return false;
	}

	@NotNull
	@Override
	public Iterator<BlockPos> iterator() {
		return new PipeRouter.SingleUnitIterator(this.core);
	}
}
