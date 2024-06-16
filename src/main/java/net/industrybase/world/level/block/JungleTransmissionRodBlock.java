package net.industrybase.world.level.block;

import com.mojang.serialization.MapCodec;
import net.industrybase.api.transmit.WoodTransmissionRod;
import net.industrybase.world.level.block.entity.JungleTransmissionRodBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class JungleTransmissionRodBlock extends WoodTransmissionRod {
	public static final MapCodec<JungleTransmissionRodBlock> CODEC = simpleCodec((properties) -> new JungleTransmissionRodBlock());

	public JungleTransmissionRodBlock() {
		super(Blocks.JUNGLE_LOG);
	}

	@Nullable
	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new JungleTransmissionRodBlockEntity(pos, state);
	}

	@Override
	protected MapCodec<? extends BaseEntityBlock> codec() {
		return CODEC;
	}
}
