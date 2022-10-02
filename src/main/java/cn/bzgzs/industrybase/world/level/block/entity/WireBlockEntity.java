package cn.bzgzs.industrybase.world.level.block.entity;

import cn.bzgzs.industrybase.api.world.level.block.entity.BaseElectricBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class WireBlockEntity extends BaseElectricBlockEntity {
	public WireBlockEntity(BlockPos pos, BlockState blockState) {
		super(BlockEntityTypeList.WIRE.get(), pos, blockState);
	}
}
