package net.industrybase.api.pipe;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import org.jetbrains.annotations.Nullable;

import java.util.EnumMap;
import java.util.function.Consumer;

public class PipeRouter implements IPipeUnit {
	private final BlockPos core;
	private final EnumMap<Direction, IPipeUnit> neighbors = new EnumMap<>(Direction.class);

	public PipeRouter(BlockPos pos) {
		this.core = pos.immutable();
	}

	@Override
	public int size() {
		return 1;
	}

	@Override
	public boolean addPipe(BlockPos pos) {
		throw new UnsupportedOperationException(); 
	}

	@Nullable
	@Override
	public PipeUnit link(IPipeUnit unit) {
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
		IPipeUnit neighbor = this.neighbors.remove(direction);
		if (neighbor != null) neighbor.setNeighbor(direction.getOpposite(), null);
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
	public IPipeUnit getNeighbor(Direction direction) {
		return this.neighbors.get(direction);
	}

	@Override
	public void setNeighbor(Direction direction, @Nullable IPipeUnit neighbor) {
		this.neighbors.put(direction, neighbor);
	}

	@Override
	public void forEachNeighbor(Consumer<? super IPipeUnit> action) {
		this.neighbors.values().forEach(action);
	}

	@Override
	public boolean isRouter() {
		return true;
	}

	@Override
	public boolean isSingle() {
		return true;
	}
}
