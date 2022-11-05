package cn.bzgzs.industrybase.world.level.block;

import cn.bzgzs.industrybase.world.level.block.entity.JungleTransmissionRodBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class JungleTransmissionRodBlock extends WoodTransmissionRod {
	public JungleTransmissionRodBlock() {
		super(Blocks.JUNGLE_LOG);
	}

	@Nullable
	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new JungleTransmissionRodBlockEntity(pos, state);
	}
}
