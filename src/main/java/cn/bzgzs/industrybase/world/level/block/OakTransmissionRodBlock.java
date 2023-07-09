package cn.bzgzs.industrybase.world.level.block;

import cn.bzgzs.industrybase.api.transmit.WoodTransmissionRod;
import cn.bzgzs.industrybase.world.level.block.entity.OakTransmissionRodBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class OakTransmissionRodBlock extends WoodTransmissionRod {
	public OakTransmissionRodBlock() {
		super(Blocks.OAK_LOG);
	}

	@Nullable
	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new OakTransmissionRodBlockEntity(pos, state);
	}
}
