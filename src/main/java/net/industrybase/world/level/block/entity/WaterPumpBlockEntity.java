package net.industrybase.world.level.block.entity;

import net.industrybase.api.pipe.PipeConnectedHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class WaterPumpBlockEntity extends BlockEntity {
	private final PipeConnectedHandler handler = new PipeConnectedHandler(this);

	public WaterPumpBlockEntity(BlockPos pos, BlockState state) {
		super(BlockEntityTypeList.WATER_PUMP.get(), pos, state);
	}
}
