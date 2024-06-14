package net.industrybase.world.level.block;

import net.industrybase.api.transmit.WoodTransmissionRod;
import net.industrybase.world.level.block.entity.CherryTransmissionRodBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class CherryTransmissionRodBlock extends WoodTransmissionRod {
	public CherryTransmissionRodBlock() {
		super(Blocks.CHERRY_LOG);
	}

	@Nullable
	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new CherryTransmissionRodBlockEntity(pos, state);
	}
}
