package cn.bzgzs.industrybase.world.level.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class DarkOakTransmissionRodBlockEntity extends TransmissionRodBlockEntity {
	public DarkOakTransmissionRodBlockEntity(BlockPos pos, BlockState state) {
		super(BlockEntityTypeList.DARK_OAK_TRANSMISSION_ROD.get(), pos, state);
	}
}
