package net.industrybase.world.level.block.entity;

import net.industrybase.api.transmit.TransmissionRodBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class AcaciaTransmissionRodBlockEntity extends TransmissionRodBlockEntity {
	public AcaciaTransmissionRodBlockEntity(BlockPos pos, BlockState state) {
		super(BlockEntityTypeList.ACACIA_TRANSMISSION_ROD.get(), pos, state);
	}
}
