package cn.bzgzs.industrybase.world.level.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class IronTransmissionRodBlockEntity extends TransmissionRodBlockEntity {
	public IronTransmissionRodBlockEntity(BlockPos pos, BlockState state) {
		super(BlockEntityTypeList.IRON_TRANSMISSION_ROD.get(), pos, state);
	}
}
