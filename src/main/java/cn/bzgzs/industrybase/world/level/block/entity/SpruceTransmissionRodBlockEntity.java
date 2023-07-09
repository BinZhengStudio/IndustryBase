package cn.bzgzs.industrybase.world.level.block.entity;

import cn.bzgzs.industrybase.api.transmit.TransmissionRodBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class SpruceTransmissionRodBlockEntity extends TransmissionRodBlockEntity {
	public SpruceTransmissionRodBlockEntity(BlockPos pos, BlockState state) {
		super(BlockEntityTypeList.SPRUCE_TRANSMISSION_ROD.get(), pos, state);
	}
}
