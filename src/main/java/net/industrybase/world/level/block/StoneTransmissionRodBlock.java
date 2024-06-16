package net.industrybase.world.level.block;

import com.mojang.serialization.MapCodec;
import net.industrybase.api.transmit.TransmissionRodBlock;
import net.industrybase.world.level.block.entity.StoneTransmissionRodBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class StoneTransmissionRodBlock extends TransmissionRodBlock {
	public static final MapCodec<StoneTransmissionRodBlock> CODEC = simpleCodec((properties) -> new StoneTransmissionRodBlock());

	public StoneTransmissionRodBlock() {
		super(Properties.ofFullCopy(Blocks.STONE).noOcclusion(), 10);
	}

	@Nullable
	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new StoneTransmissionRodBlockEntity(pos, state);
	}

	@Override
	protected MapCodec<? extends BaseEntityBlock> codec() {
		return CODEC;
	}
}
