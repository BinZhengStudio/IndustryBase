package net.industrybase.api.pipe;

import net.minecraft.core.BlockPos;

public class PipeUnit extends BlockPos {
//	private final Direction.Axis axis;
//	private final int start;
//	private final int end;

	public PipeUnit(int x, int y, int z) {
		super(x, y, z);
	}

	public PipeUnit(BlockPos pos) {
		super(pos.getX(), pos.getY(), pos.getZ());
	}
}
