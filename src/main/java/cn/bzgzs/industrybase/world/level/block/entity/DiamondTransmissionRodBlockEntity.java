package cn.bzgzs.industrybase.world.level.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class DiamondTransmissionRodBlockEntity extends TransmissionRodBlockEntity {
	public DiamondTransmissionRodBlockEntity(BlockPos pos, BlockState state) {
		super(BlockEntityTypeList.DIAMOND_TRANSMISSION_ROD.get(), pos, state);
	}
}
