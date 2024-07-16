package net.industrybase.world.level.block.entity;

import net.industrybase.api.transmit.MechanicalTransmit;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;

public class AxisConnectorBlockEntity extends BlockEntity {
	private final MechanicalTransmit transmit = new MechanicalTransmit(this);

	public AxisConnectorBlockEntity(BlockPos pos, BlockState state) {
		super(BlockEntityTypeList.AXIS_CONNECTOR.get(), pos, state);
	}

	@Override
	public void onLoad() {
		super.onLoad();
		this.transmit.register();
	}

	@Nullable
	public MechanicalTransmit getTransmit(Direction side) {
		return this.transmit;
	}

	@Override
	public void setRemoved() {
		this.transmit.remove();
		super.setRemoved();
	}
}
