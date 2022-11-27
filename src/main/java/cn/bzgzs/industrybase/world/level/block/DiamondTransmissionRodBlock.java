package cn.bzgzs.industrybase.world.level.block;

import cn.bzgzs.industrybase.world.level.block.entity.DiamondTransmissionRodBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class DiamondTransmissionRodBlock extends LayeredTransmissionRodBlock {
	public DiamondTransmissionRodBlock() {
		super(Properties.copy(Blocks.IRON_BLOCK), 100, 0x3B, 0xD4, 0xD4);
	}

	@Nullable
	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new DiamondTransmissionRodBlockEntity(pos, state);
	}
}
