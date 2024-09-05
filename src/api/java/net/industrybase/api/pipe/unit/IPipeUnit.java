package net.industrybase.api.pipe.unit;

import net.industrybase.api.pipe.PipeNetwork;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.common.NeoForgeMod;

import javax.annotation.Nullable;
import java.util.ArrayDeque;
import java.util.function.BiConsumer;

public interface IPipeUnit extends Iterable<BlockPos> {
	int size();

	int getMaxTick();

	double getPressure(Direction direction);

	void setPressure(ArrayDeque<Runnable> tasks, ArrayDeque<Runnable> next, Direction direction, double newPressure);

	default void onNeighborUpdatePressure(ArrayDeque<Runnable> tasks, ArrayDeque<Runnable> next, IPipeUnit neighbor, Direction direction, double neighborPressure) {
		Direction neighborFace = direction.getOpposite();
		double speed = this.getSpeed(direction, neighbor, neighborPressure);

		int maxAmount = this.applySpeed(direction, speed, true);
		int neighborMaxAmount = -neighbor.addAmount(neighborFace, -maxAmount, true);
		int amount = speed > 0 ? Math.min(maxAmount, neighborMaxAmount) : Math.max(maxAmount, neighborMaxAmount);

		this.addAmount(direction, amount, false);
		neighbor.addAmount(neighborFace, -amount, false);

		this.addTick(tasks, next, direction, speed);
		neighbor.addTick(tasks, next, neighborFace, -speed);
	}

	int getAmount();

	int addAmount(Direction direction, int amount, boolean simulate);

	int applySpeed(Direction direction, double speed, boolean simulate);

	double getTick(Direction direction);

	void addTick(ArrayDeque<Runnable> tasks, ArrayDeque<Runnable> next, Direction direction, double tick);

	AABB getAABB();

	int getCapacity();

	boolean addPipe(BlockPos pos);

	IPipeUnit spilt(BlockPos pos, Direction direction);

	@Nullable
	Direction.Axis getAxis();

	BlockPos getCore();

//	boolean contains(BlockPos pos);

//	int getNeighborSize();
	
	@Nullable
	IPipeUnit getNeighbor(Direction direction);

	/**
	 * set neighbor
	 * @param direction the direction
	 * @param neighbor the neighbor
	 * @return old neighbor
	 */
	@Nullable
	IPipeUnit setNeighbor(Direction direction, @Nullable IPipeUnit neighbor);

	void forEachNeighbor(BiConsumer<? super Direction, ? super IPipeUnit> action);

	UnitType getType();

	boolean isSingle();

	boolean canMergeWith(Direction direction);

	default double getSpeed(Direction direction, IPipeUnit neighbor, double neighborPressure) {
		AABB aabb = this.getAABB();
		AABB neighborAABB = neighbor.getAABB();
		double pressure = this.getPressure(direction);
		int density = NeoForgeMod.WATER_TYPE.value().getDensity();
		double square = PipeNetwork.square(direction.getAxis(), aabb, neighborAABB);

		double pressureDiff = neighborPressure - pressure;
		return (pressureDiff / density) * square * 50000.0D;
	}
}
