package cn.bzgzs.industrybase.world.level.block;

import cn.bzgzs.industrybase.world.level.block.entity.StoneTransmissionRodBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class StoneTransmissionRodBlock extends TransmissionRodBlock {
	public StoneTransmissionRodBlock() {
		super(Properties.copy(Blocks.STONE).noOcclusion(), 10);
	}

	@Nullable
	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new StoneTransmissionRodBlockEntity(pos, state);
	}
}
