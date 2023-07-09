package cn.bzgzs.industrybase.world.level.block.entity;

import cn.bzgzs.industrybase.api.transmit.TransmissionRodBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class BirchTransmissionRodBlockEntity extends TransmissionRodBlockEntity {
	public BirchTransmissionRodBlockEntity(BlockPos pos, BlockState state) {
		super(BlockEntityTypeList.BIRCH_TRANSMISSION_ROD.get(), pos, state);
	}
}
