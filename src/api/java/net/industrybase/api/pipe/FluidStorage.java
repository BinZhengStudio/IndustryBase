package net.industrybase.api.pipe;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

public class FluidStorage implements IPipeUnit{
	@Override
	public int size() {
		return 0;
	}

	@Override
	public boolean addPipe(BlockPos pos) {
		return false;
	}

	@Nullable
	@Override
	public PipeUnit link(IPipeUnit unit) {
		return null;
	}

	@Nullable
	@Override
	public PipeUnit cut(BlockPos pos) {
		return null;
	}

	@Nullable
	@Override
	public PipeUnit spilt(BlockPos pos, Direction direction) {
		return null;
	}

	@Override
	public Direction.Axis getAxis() {
		return null;
	}

	@Override
	public BlockPos getCore() {
		return null;
	}

	@Nullable
	@Override
	public IPipeUnit getNeighbor(Direction direction) {
		return null;
	}

	@Override
	public void setNeighbor(Direction direction, @Nullable IPipeUnit neighbor) {

	}

	@Override
	public void forEachNeighbor(Consumer<? super IPipeUnit> action) {

	}

	@Override
	public boolean isRouter() {
		return false;
	}

	@Override
	public boolean isSingle() {
		return false;
	}
}
