package net.industrybase.world.level.block.entity;

import net.industrybase.api.transmit.TransmissionRodBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class CrimsonTransmissionRodBlockEntity extends TransmissionRodBlockEntity {
	public CrimsonTransmissionRodBlockEntity(BlockPos pos, BlockState state) {
		super(BlockEntityTypeList.CRIMSON_TRANSMISSION_ROD.get(), pos, state);
	}
}
