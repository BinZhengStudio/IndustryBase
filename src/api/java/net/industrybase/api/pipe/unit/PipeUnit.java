package net.industrybase.api.pipe.unit;

import net.industrybase.api.pipe.PipeNetwork;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.common.NeoForgeMod;

import javax.annotation.Nullable;
import java.util.ArrayDeque;
import java.util.function.BiConsumer;

public abstract class PipeUnit implements Iterable<BlockPos> {
	private boolean ticked;
	protected final BlockPos core;

	protected PipeUnit(BlockPos core) {
		this.core = core.immutable();
	}

	public abstract int size();

	public abstract int getMaxTick();

	public abstract double getPressure(Direction direction);

	public abstract void setPressure(ArrayDeque<PipeUnit> tasks, ArrayDeque<PipeUnit> next, Direction direction, double newPressure);

	public void onNeighborUpdatePressure(ArrayDeque<PipeUnit> tasks, ArrayDeque<PipeUnit> next, PipeUnit neighbor, Direction direction, double neighborPressure) {
		Direction neighborFace = direction.getOpposite();
		double speed = this.getSpeed(direction, neighbor, neighborPressure);

		int maxAmount = this.applySpeed(direction, speed, true);
		int neighborMaxAmount = -neighbor.addAmount(neighborFace, -maxAmount, true);
		int amount = speed > 0 ? Math.min(maxAmount, neighborMaxAmount) : Math.max(maxAmount, neighborMaxAmount);

		this.addAmount(direction, amount, false); // add amount first, because addTick may use latest amount
		this.addTick(tasks, next, direction, speed);

		// latter is neighbor, in order to prevent neighbor task cut in task queue
		neighbor.addAmount(neighborFace, -amount, false);
		neighbor.addTick(tasks, next, neighborFace, -speed);
	}

	public abstract int getAmount();

	public abstract int addAmount(Direction direction, int amount, boolean simulate);

	public abstract int applySpeed(Direction direction, double speed, boolean simulate);

	public abstract double getTick(Direction direction);

	public abstract void addTick(ArrayDeque<PipeUnit> tasks, ArrayDeque<PipeUnit> next, Direction direction, double tick);

	public abstract AABB getAABB();

	public abstract int getCapacity();

	public abstract boolean addPipe(BlockPos pos);

	public abstract PipeUnit spilt(BlockPos pos, Direction direction);

	@Nullable
	public abstract Direction.Axis getAxis();

	public BlockPos getCore() {
		return this.core;
	}

//	boolean contains(BlockPos pos);

//	int getNeighborSize();
	
	@Nullable
	public abstract PipeUnit getNeighbor(Direction direction);

	/**
	 * set neighbor
	 * @param direction the direction
	 * @param neighbor the neighbor
	 * @return old neighbor
	 */
	@Nullable
	public abstract PipeUnit setNeighbor(Direction direction, @Nullable PipeUnit neighbor);

	public abstract void forEachNeighbor(BiConsumer<? super Direction, ? super PipeUnit> action);

	public abstract void tickTasks();

	public boolean ticked() {
		return this.ticked;
	}

	public void unsetTicked() {
		this.ticked = false;
	}

	public void setTicked() {
		this.ticked = true;
	}

	public abstract UnitType getType();

	public abstract boolean isSingle();

	public abstract boolean canMergeWith(Direction direction);

	public double getSpeed(Direction direction, PipeUnit neighbor, double neighborPressure) {
		AABB aabb = this.getAABB();
		AABB neighborAABB = neighbor.getAABB();
		double pressure = this.getPressure(direction);
		int density = NeoForgeMod.WATER_TYPE.value().getDensity();
		double square = PipeNetwork.square(direction.getAxis(), aabb, neighborAABB);

		double pressureDiff = neighborPressure - pressure;
		return (pressureDiff / density) * square * 50000.0D;
	}
}
