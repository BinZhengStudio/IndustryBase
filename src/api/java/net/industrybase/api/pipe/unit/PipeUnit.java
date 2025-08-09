package net.industrybase.api.pipe.unit;

import net.industrybase.api.pipe.MergeCheckResult;
import net.industrybase.api.pipe.PipeNetwork;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.common.NeoForgeMod;

import javax.annotation.Nullable;
import java.util.ArrayDeque;
import java.util.function.BiConsumer;

public abstract class PipeUnit implements Iterable<BlockPos> {
	protected static final Direction[] DIRECTIONS = Direction.values();
	private boolean submittedTask;
	protected final BlockPos core;
	protected final AABB aabb;
	protected final PipeNetwork network;

	protected PipeUnit(PipeNetwork network, BlockPos core, AABB aabb) {
		this.core = core.immutable();
		this.network = network;
		this.aabb = aabb;
	}

	public abstract int size();

	public abstract int getMaxTick();

	public abstract double getPressure(Direction direction);

	public abstract void setPressure(ArrayDeque<PipeUnit> tasks, Direction direction, double newPressure);

	public void onNeighborUpdatePressure(PipeUnit neighbor, Direction direction, double neighborPressure) {
		Direction neighborFace = direction.getOpposite();
		double speed = this.getSpeed(direction, neighbor, neighborPressure);

		int maxAmount = this.applySpeed(direction, speed, true);
		int neighborMaxAmount = -neighbor.addAmount(neighborFace, -maxAmount, true);
		int amount = speed > 0 ? Math.min(maxAmount, neighborMaxAmount) : Math.max(maxAmount, neighborMaxAmount);

		this.addAmount(direction, amount, false); // add amount first, because addTick may use latest amount
		this.addTick(direction, speed);

		// latter is neighbor, in order to prevent neighbor task cut in task queue
		neighbor.addAmount(neighborFace, -amount, false);
		neighbor.addTick(neighborFace, -speed);
	}

	public abstract int getAmount();

	public abstract int addAmount(Direction direction, int amount, boolean simulate);

	public int applySpeed(Direction direction, double speed, boolean simulate) {
		return this.addAmount(direction, (int) (speed * 20), simulate);
	}

	public abstract double getTick(Direction direction);

	protected abstract void setTick(Direction direction, double tick);

	public abstract void addTick(Direction direction, double tick);

	public abstract int getCapacity();

	public abstract boolean addPipe(BlockPos pos);

	public abstract PipeUnit spilt(BlockPos pos, Direction direction);

	@Nullable
	public abstract Direction.Axis getAxis();

	public BlockPos getCore() {
		return this.core;
	}

	public AABB getAABB() {
		return this.aabb;
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


	public boolean submittedTask() {
		return this.submittedTask;
	}

	public void setSubmittedTask() {
		this.submittedTask = true;
	}

	public void unsetSubmittedTask() {
		this.submittedTask = false;
	}

	public abstract UnitType getType();

	public abstract boolean isSingle();

	public abstract MergeCheckResult canMergeWith(Direction direction, AABB neighborAABB);

	public double getSpeed(Direction direction, PipeUnit neighbor, double neighborPressure) {
		AABB aabb = this.aabb;
		AABB neighborAABB = neighbor.aabb;
		double pressure = this.getPressure(direction);
		int density = NeoForgeMod.WATER_TYPE.value().getDensity();
		double factor = factor(direction, aabb, neighborAABB);

		double pressureDiff = neighborPressure - pressure;
		return (pressureDiff / density) * factor * 50000.0D;
	}

	public static double factor(Direction direction, AABB aabb1, AABB aabb2) {
		Direction.Axis axis = direction.getAxis();
		Vec3i normal = direction.getNormal();
		aabb2 = aabb2.move(normal.getX(), normal.getY(), normal.getZ());

		double x = Math.min(aabb1.maxX, aabb2.maxX) - Math.max(aabb1.minX, aabb2.minX);
		double y = Math.min(aabb1.maxY, aabb2.maxY) - Math.max(aabb1.minY, aabb2.minY);
		double z = Math.min(aabb1.maxZ, aabb2.maxZ) - Math.max(aabb1.minZ, aabb2.minZ);

		double distance;
		double minArea;
		double maxArea;

		switch (axis) {
			case X -> {
				distance = Math.max(-x, 0.0D);
				maxArea = Math.min(aabb1.getYsize() * aabb1.getZsize(), aabb2.getYsize() * aabb2.getZsize());
				if (y < 0.0D || z < 0.0D) {
					minArea = -Math.abs(y * z); // make sure minArea is negative
				} else {
					minArea = y * z;
				}
			}
			case Y -> {
				distance = Math.max(-y, 0.0D);
				maxArea = Math.min(aabb1.getXsize() * aabb1.getZsize(), aabb2.getXsize() * aabb2.getZsize());
				if (x < 0.0D || z < 0.0D) {
					minArea = -Math.abs(x * z);
				} else {
					minArea = x * z;
				}
			}
			default -> {
				distance = Math.max(-z, 0.0D);
				maxArea = Math.min(aabb1.getXsize() * aabb1.getYsize(), aabb2.getXsize() * aabb2.getYsize());
				if (x < 0.0D || y < 0.0D) {
					minArea = -Math.abs(x * y);
				} else {
					minArea = x * y;
				}
			}
		}

		double diff = maxArea - minArea;

		if (diff <= 0.0D) return maxArea;
		return Math.max(maxArea - (diff / (distance + (diff / maxArea))), 0.0D);
	}
}
