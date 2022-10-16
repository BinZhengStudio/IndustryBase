package cn.bzgzs.industrybase.world.level.block;

import cn.bzgzs.industrybase.world.level.block.entity.IronTransmissionRodBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class IronTransmissionRodBlock extends TransmissionRodBlock {
	public IronTransmissionRodBlock() {
		super(Properties.copy(Blocks.IRON_BLOCK).noOcclusion(), 50); // TODO
	}

	@Nullable
	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new IronTransmissionRodBlockEntity(pos, state);
	}
}
