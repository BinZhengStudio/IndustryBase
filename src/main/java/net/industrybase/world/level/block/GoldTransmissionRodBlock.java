package net.industrybase.world.level.block;

import com.mojang.serialization.MapCodec;
import net.industrybase.api.transmit.LayeredTransmissionRodBlock;
import net.industrybase.world.level.block.entity.GoldTransmissionRodBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class GoldTransmissionRodBlock extends LayeredTransmissionRodBlock {
	public static final MapCodec<GoldTransmissionRodBlock> CODEC = simpleCodec((properties) -> new GoldTransmissionRodBlock());

	public GoldTransmissionRodBlock() {
		super(Properties.ofFullCopy(Blocks.IRON_BLOCK), 50, 0xFF, 0xE0, 0x00);
	}

	@Nullable
	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new GoldTransmissionRodBlockEntity(pos, state);
	}

	@Override
	protected MapCodec<? extends BaseEntityBlock> codec() {
		return CODEC;
	}
}
