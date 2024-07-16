package net.industrybase.world.level.block.entity;

import net.industrybase.api.electric.ElectricPower;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class WireBlockEntity extends BlockEntity {
	private final ElectricPower electricPower = new ElectricPower(this);

	public WireBlockEntity(BlockPos pos, BlockState blockState) {
		super(BlockEntityTypeList.WIRE.get(), pos, blockState);
	}

	@Override
	public void onLoad() {
		super.onLoad();
		this.electricPower.register();
	}

	public ElectricPower getElectricPower(Direction side) {
		return this.electricPower;
	}

	@Override
	public void setRemoved() {
		this.electricPower.remove();
		super.setRemoved();
	}
}
