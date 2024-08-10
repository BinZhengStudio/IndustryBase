package net.industrybase.api.pipe.unit;

import net.industrybase.api.pipe.PipeNetwork;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.common.NeoForgeMod;

import javax.annotation.Nullable;
import java.util.ArrayDeque;
import java.util.function.BiConsumer;

public interface IPipeUnit {
	int size();

	int getMaxTick();

	double getPressure(Direction direction);

	boolean setPressure(ArrayDeque<Runnable> tasks, ArrayDeque<Runnable> next, Direction direction, double pressure);

	default void onNeighborUpdatePressure(ArrayDeque<Runnable> tasks, ArrayDeque<Runnable> next, IPipeUnit neighbor, Direction direction, double neighborPressure) {
		Direction neighborFace = direction.getOpposite();
		double speed = this.getSpeed(direction, neighbor, neighborPressure);

		tasks.addLast(() -> {
			int maxAmount = this.applySpeed(direction, speed, true);
			int neighborMaxAmount = -neighbor.addAmount(neighborFace, -this.getAmount(), true);
			int amount = speed > 0 ? Math.min(maxAmount, neighborMaxAmount) : Math.max(maxAmount, neighborMaxAmount);

			this.addAmount(direction, amount, false);
			neighbor.addAmount(neighborFace, -amount, false);

			this.addTick(tasks, next, direction, speed);
			neighbor.addTick(tasks, next, neighborFace, -speed);
		});
	}

	int getAmount();

	int addAmount(Direction direction, int amount, boolean simulate);

	int applySpeed(Direction direction, double speed, boolean simulate);

	double getTick(Direction direction);

	void addTick(ArrayDeque<Runnable> tasks, ArrayDeque<Runnable> next, Direction direction, double tick);

	AABB getAABB();

	int getCapacity();

	boolean addPipe(BlockPos pos);

	@Nullable
	PipeUnit link(IPipeUnit neighbor);
	
	@Nullable
	PipeUnit cut(BlockPos pos);

	@Nullable
	PipeUnit spilt(BlockPos pos, Direction direction);
	
	Direction.Axis getAxis();

	BlockPos getCore();

	boolean contains(BlockPos pos);

//	int getNeighborSize();
	
	@Nullable
	IPipeUnit getNeighbor(Direction direction);

	@Nullable
	IPipeUnit setNeighbor(Direction direction, @Nullable IPipeUnit neighbor);

	void forEachNeighbor(BiConsumer<? super Direction, ? super IPipeUnit> action);

	boolean isUnit();

	boolean isRouter();

	boolean isSingle();

	boolean isStorage();

	default double getSpeed(Direction direction, IPipeUnit neighbor, double neighborPressure) {
		AABB aabb = this.getAABB();
		AABB neighborAABB = neighbor.getAABB();
		double pressure = this.getPressure(direction);
		int density = NeoForgeMod.WATER_TYPE.value().getDensity();
		double square = PipeNetwork.square(direction.getAxis(), aabb, neighborAABB);

		double pressureDiff = neighborPressure - pressure;
		return pressureDiff / density * square * 1000.0D;
	}
}
