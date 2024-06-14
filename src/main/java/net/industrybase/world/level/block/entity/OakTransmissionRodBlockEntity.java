package net.industrybase.world.level.block.entity;

import net.industrybase.api.transmit.TransmissionRodBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class OakTransmissionRodBlockEntity extends TransmissionRodBlockEntity {
	public OakTransmissionRodBlockEntity(BlockPos pos, BlockState state) {
		super(BlockEntityTypeList.OAK_TRANSMISSION_ROD.get(), pos, state);
	}
}
