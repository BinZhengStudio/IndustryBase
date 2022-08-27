package cn.bzgzs.largeprojects.world.level.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class TransmissionRodBlockEntity extends BlockEntity {
	public TransmissionRodBlockEntity(BlockPos pos, BlockState state) {
		super(BlockEntityTypeList.TRANSMISSION_ROD.get(), pos, state);
	}
}
