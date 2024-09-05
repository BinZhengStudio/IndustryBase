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

public class FluidStorage extends PipeUnit {
	protected final StorageInterface storageInterface;
	protected final AABB aabb; // TODO
	protected final PipeUnit[] neighbors = new PipeUnit[6];
	private final Runnable[] tasks = new Runnable[6];
	protected final double[] pressure = new double[6];

	public FluidStorage(BlockPos core, StorageInterface storageInterface) {
		super(core);
		this.storageInterface = storageInterface;
		this.aabb = new AABB(core);
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
	public void addTick(ArrayDeque<PipeUnit> tasks, ArrayDeque<PipeUnit> next, Direction direction, double tick) {
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

	@Nullable
	@Override
	public PipeUnit getNeighbor(Direction direction) {
		return this.neighbors[direction.ordinal()];
	}

	@Override
	public PipeUnit setNeighbor(Direction direction, @Nullable PipeUnit neighbor) {
		int index = direction.ordinal();
		PipeUnit old = this.neighbors[index];
		this.neighbors[index] = neighbor;
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
