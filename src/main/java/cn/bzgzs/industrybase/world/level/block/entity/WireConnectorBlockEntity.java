package cn.bzgzs.industrybase.world.level.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class WireConnectorBlockEntity extends BlockEntity {
	public WireConnectorBlockEntity(BlockPos pos, BlockState state) {
		super(BlockEntityTypeList.WIRE_CONNECTOR.get(), pos, state);
	}
}
