package net.industrybase.api.pipe;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

import javax.annotation.Nullable;
import java.util.function.Consumer;

public interface IPipeUnit {
	int size();
	
	boolean addPipe(BlockPos pos);

	@Nullable
	PipeUnit link(IPipeUnit unit);
	
	@Nullable
	PipeUnit cut(BlockPos pos);

	@Nullable
	PipeUnit spilt(BlockPos pos, Direction direction);
	
	Direction.Axis getAxis();

	BlockPos getCore();
	
	@Nullable
	IPipeUnit getNeighbor(Direction direction);

	void setNeighbor(Direction direction, @Nullable IPipeUnit neighbor);

	void forEachNeighbor(Consumer<? super IPipeUnit> action);

	boolean isRouter();

	boolean isSingle();
}
