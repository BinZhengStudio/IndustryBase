package net.industrybase.world.level.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class CreativeElectricMotorBlockEntity extends ElectricMotorBlockEntity {
	public CreativeElectricMotorBlockEntity(BlockPos pos, BlockState state) {
		super(BlockEntityTypeList.CREATIVE_ELECTRIC_MOTOR.get(), pos, state);
	}

	@Override
	public void onLoad() {
		super.onLoad();
		this.transmit.setPower(32); // 2 EP 对应约 32 ME
        this.electricPower.setInputPower(0.0D);
	}
}
