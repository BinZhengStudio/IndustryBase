package net.industrybase.world.level.block;

import com.mojang.serialization.MapCodec;
import net.industrybase.api.transmit.WoodTransmissionRod;
import net.industrybase.world.level.block.entity.MangroveTransmissionRodBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class MangroveTransmissionRodBlock extends WoodTransmissionRod {
	public static final MapCodec<MangroveTransmissionRodBlock> CODEC = simpleCodec((properties) -> new MangroveTransmissionRodBlock());

	public MangroveTransmissionRodBlock() {
		super(Blocks.MANGROVE_LOG);
	}

	@Nullable
	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new MangroveTransmissionRodBlockEntity(pos, state);
	}

	@Override
	protected MapCodec<? extends BaseEntityBlock> codec() {
		return CODEC;
	}
}
