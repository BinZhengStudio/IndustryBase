package net.industrybase.world.level.block;

import com.mojang.serialization.MapCodec;

import net.industrybase.world.level.block.entity.TransmissionRodBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class DiamondTransmissionRodBlock extends LayeredTransmissionRodBlock {
	public static final MapCodec<DiamondTransmissionRodBlock> CODEC = simpleCodec((properties) -> new DiamondTransmissionRodBlock());

	public DiamondTransmissionRodBlock() {
		super(Properties.ofFullCopy(Blocks.IRON_BLOCK), 100, 0x3BD4D4);
	}

	@Nullable
	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new TransmissionRodBlockEntity(pos, state);
	}

	@Override
	protected MapCodec<? extends BaseEntityBlock> codec() {
		return CODEC;
	}
}
