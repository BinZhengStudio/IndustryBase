package net.industrybase.world.level.block;

import com.mojang.serialization.MapCodec;
import net.industrybase.api.pipe.PipeBlock;
import net.industrybase.world.level.block.entity.IronPipeBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class IronPipeBlock extends PipeBlock {
	public static final MapCodec<IronPipeBlock> CODEC = simpleCodec((properties) -> new IronPipeBlock());

	protected IronPipeBlock() {
		super(Properties.ofFullCopy(Blocks.IRON_BLOCK));
	}

	@Override
	protected MapCodec<? extends BaseEntityBlock> codec() {
		return CODEC;
	}

	@Nullable
	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new IronPipeBlockEntity(pos, state);
	}
}
