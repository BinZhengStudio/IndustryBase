package net.industrybase.world.level.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class CreativeDynamoBlockEntity extends DynamoBlockEntity {
	public CreativeDynamoBlockEntity(BlockPos pos, BlockState state) {
		super(BlockEntityTypeList.CREATIVE_DYNAMO.get(), pos, state);
	}

	@Override
	public void onLoad() {
		super.onLoad();
		this.electricPower.setOutputPower(10.0D);
	}
}
