package net.industrybase.world.level.block;

import com.mojang.serialization.MapCodec;
import net.industrybase.api.transmit.WoodTransmissionRod;
import net.industrybase.world.level.block.entity.BirchTransmissionRodBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class BirchTransmissionRodBlock extends WoodTransmissionRod {
	public static final MapCodec<BirchTransmissionRodBlock> CODEC = simpleCodec((properties) -> new BirchTransmissionRodBlock());

	public BirchTransmissionRodBlock() {
		super(Blocks.BIRCH_LOG);
	}

	@Nullable
	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new BirchTransmissionRodBlockEntity(pos, state);
	}

	@Override
	protected MapCodec<? extends BaseEntityBlock> codec() {
		return CODEC;
	}
}
