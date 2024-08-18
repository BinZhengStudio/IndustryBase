package net.industrybase.api.pipe.unit;

import it.unimi.dsi.fastutil.objects.ObjectIterators;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayDeque;
import java.util.Iterator;
import java.util.function.BiConsumer;

public class EmptyUnit implements IPipeUnit {
	protected static final EmptyUnit INSTANCE = new EmptyUnit();
	protected static final EmptyUnit[] INSTANCES = new EmptyUnit[]{INSTANCE};
	private final AABB aabb = new AABB(0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D);

	private EmptyUnit() {
	}

	@Override
	public int size() {
		return 0;
	}

	@Override
	public int getMaxTick() {
		return 0;
	}

	@Override
	public double getPressure(Direction direction) {
		return 0;
	}

	@Override
	public boolean setPressure(ArrayDeque<Runnable> tasks, ArrayDeque<Runnable> next, Direction direction, double pressure) {
		return false;
	}

	@Override
	public int getAmount() {
		return 0;
	}

	@Override
	public int addAmount(Direction direction, int amount, boolean simulate) {
		return 0;
	}

	@Override
	public int applySpeed(Direction direction, double speed, boolean simulate) {
		return 0;
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
		return 0;
	}

	@Override
	public boolean addPipe(BlockPos pos) {
		return false;
	}

	@Override
	public IPipeUnit spilt(BlockPos pos, Direction direction) {
		return this;
	}

	@Nullable
	@Override
	public Direction.Axis getAxis() {
		return null;
	}

	@Override
	public BlockPos getCore() {
		return BlockPos.ZERO;
	}

	@Nullable
	@Override
	public IPipeUnit getNeighbor(Direction direction) {
		return null;
	}

	@Nullable
	@Override
	public IPipeUnit setNeighbor(Direction direction, @Nullable IPipeUnit neighbor) {
		return null;
	}

	@Override
	public void forEachNeighbor(BiConsumer<? super Direction, ? super IPipeUnit> action) {
	}

	@Override
	public UnitType getType() {
		return UnitType.EMPTY;
	}

	@Override
	public boolean isSingle() {
		return false;
	}

	@Override
	public boolean canMergeWith(Direction direction) {
		return false;
	}

	@NotNull
	@Override
	public Iterator<BlockPos> iterator() {
		return new EmptyIterator();
	}

	private static class EmptyIterator extends ObjectIterators.EmptyIterator<BlockPos> {
		private EmptyIterator() {
			super();
		}
	}
}
