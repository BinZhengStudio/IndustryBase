package net.industrybase.world.level.block;

import com.mojang.serialization.MapCodec;

import net.industrybase.world.level.block.entity.TransmissionRodBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class GoldTransmissionRodBlock extends LayeredTransmissionRodBlock {
	public static final MapCodec<GoldTransmissionRodBlock> CODEC = simpleCodec(GoldTransmissionRodBlock::new);

	public GoldTransmissionRodBlock(Properties properties) {
		super(properties, 50, 0xFFE000);
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
