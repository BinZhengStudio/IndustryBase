package net.industrybase.world.level.block;

import net.industrybase.world.level.block.entity.AxisConnectorBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class AxisConnectorBlock extends BaseEntityBlock {
	protected AxisConnectorBlock() {
		super(Properties.copy(Blocks.IRON_BLOCK).noOcclusion());
	}

	@Override
	@SuppressWarnings("deprecation")
	public RenderShape getRenderShape(BlockState state) {
		return RenderShape.MODEL;
	}

	@Nullable
	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new AxisConnectorBlockEntity(pos, state);
	}
}
