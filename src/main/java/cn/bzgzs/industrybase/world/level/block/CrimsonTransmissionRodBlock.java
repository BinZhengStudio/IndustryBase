package cn.bzgzs.industrybase.world.level.block;

import cn.bzgzs.industrybase.api.transmit.WoodTransmissionRod;
import cn.bzgzs.industrybase.world.level.block.entity.CrimsonTransmissionRodBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class CrimsonTransmissionRodBlock extends WoodTransmissionRod {
	public CrimsonTransmissionRodBlock() {
		super(Blocks.CRIMSON_STEM);
	}

	@Nullable
	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new CrimsonTransmissionRodBlockEntity(pos, state);
	}
}
