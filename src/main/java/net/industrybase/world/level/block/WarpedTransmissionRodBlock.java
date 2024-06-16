package net.industrybase.world.level.block;

import com.mojang.serialization.MapCodec;
import net.industrybase.api.transmit.WoodTransmissionRod;
import net.industrybase.world.level.block.entity.WarpedTransmissionRodBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class WarpedTransmissionRodBlock extends WoodTransmissionRod {
	public static final MapCodec<WarpedTransmissionRodBlock> CODEC = simpleCodec((properties) -> new WarpedTransmissionRodBlock());

	public WarpedTransmissionRodBlock() {
		super(Blocks.CRIMSON_STEM);
	}

	@Nullable
	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new WarpedTransmissionRodBlockEntity(pos, state);
	}

	@Override
	protected MapCodec<? extends BaseEntityBlock> codec() {
		return CODEC;
	}
}
