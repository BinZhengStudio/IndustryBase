package net.industrybase.world.level.block.entity;

import net.industrybase.api.transmit.TransmissionRodBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class CherryTransmissionRodBlockEntity extends TransmissionRodBlockEntity {
	public CherryTransmissionRodBlockEntity(BlockPos pos, BlockState state) {
		super(BlockEntityTypeList.CHERRY_TRANSMISSION_ROD.get(), pos, state);
	}
}
