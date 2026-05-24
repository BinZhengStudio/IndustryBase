package net.industrybase.world.level.block;

import com.mojang.serialization.MapCodec;

import net.industrybase.api.transmit.TransmissionRodBlockEntity;
import net.industrybase.api.transmit.WoodTransmissionRod;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class OakTransmissionRodBlock extends WoodTransmissionRod {
	public static final MapCodec<OakTransmissionRodBlock> CODEC = simpleCodec((properties) -> new OakTransmissionRodBlock());

	public OakTransmissionRodBlock() {
		super(Blocks.OAK_LOG);
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
