package net.industrybase.world.level.block;

import com.mojang.serialization.MapCodec;
import net.industrybase.api.transmit.TransmissionRodBlock;
import net.industrybase.world.level.block.entity.IronTransmissionRodBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class IronTransmissionRodBlock extends TransmissionRodBlock {
	public static final MapCodec<IronTransmissionRodBlock> CODEC = simpleCodec((properties) -> new IronTransmissionRodBlock());

	public IronTransmissionRodBlock() {
		super(Properties.ofFullCopy(Blocks.IRON_BLOCK).strength(3.0F).noOcclusion(), 20);
	}

	@Nullable
	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new IronTransmissionRodBlockEntity(pos, state);
	}

	@Override
	protected MapCodec<? extends BaseEntityBlock> codec() {
		return CODEC;
	}
}
