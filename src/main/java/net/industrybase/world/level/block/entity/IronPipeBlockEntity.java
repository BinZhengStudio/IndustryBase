package net.industrybase.world.level.block.entity;

import net.industrybase.api.pipe.PipeConnectedHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class IronPipeBlockEntity extends BlockEntity {
	private final PipeConnectedHandler handler = new PipeConnectedHandler();

	public IronPipeBlockEntity(BlockPos pos, BlockState state) {
		super(BlockEntityTypeList.IRON_PIPE.get(), pos, state);
	}
}
