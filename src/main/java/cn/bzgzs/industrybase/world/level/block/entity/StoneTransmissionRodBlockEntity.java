package cn.bzgzs.industrybase.world.level.block.entity;

import cn.bzgzs.industrybase.api.transmit.TransmissionRodBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class StoneTransmissionRodBlockEntity extends TransmissionRodBlockEntity {
	public StoneTransmissionRodBlockEntity(BlockPos pos, BlockState state) {
		super(BlockEntityTypeList.STONE_TRANSMISSION_ROD.get(), pos, state);
	}
}
