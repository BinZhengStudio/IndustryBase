package cn.bzgzs.industrybase.world.level.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class MangroveTransmissionRodBlockEntity extends TransmissionRodBlockEntity {
	public MangroveTransmissionRodBlockEntity(BlockPos pos, BlockState state) {
		super(BlockEntityTypeList.MANGROVE_TRANSMISSION_ROD.get(), pos, state);
	}
}
