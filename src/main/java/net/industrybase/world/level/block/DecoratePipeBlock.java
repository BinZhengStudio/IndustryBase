package net.industrybase.world.level.block;

import com.mojang.serialization.MapCodec;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class DecoratePipeBlock extends PipeBlock {
	public static final MapCodec<DecoratePipeBlock> CODEC = simpleCodec(DecoratePipeBlock::new);

	protected DecoratePipeBlock(Properties properties) {
		super(properties);
	}

	@Override
	protected MapCodec<? extends BaseEntityBlock> codec() {
		return CODEC;
	}

	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return null;
	}
}
