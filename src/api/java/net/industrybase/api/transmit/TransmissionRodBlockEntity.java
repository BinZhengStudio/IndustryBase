package net.industrybase.api.transmit;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import org.jetbrains.annotations.Nullable;

public abstract class TransmissionRodBlockEntity extends BlockEntity {
	private final MechanicalTransmit transmit = new MechanicalTransmit(this);
	private boolean subscribed = false;

	public TransmissionRodBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
	}

	@Override
	public void onLoad() {
		super.onLoad();
		this.transmit.register();
	}

	@Nullable
	public MechanicalTransmit getTransmit(Direction side) {
		if (side.getAxis() == this.getBlockState().getValue(BlockStateProperties.AXIS)) {
			return this.transmit;
		}
		return null;
	}

	@Override
	public void setRemoved() {
		this.transmit.remove();
		super.setRemoved();
	}

	public boolean isSubscribed() {
		return this.subscribed;
	}

	public void setSubscribed() {
		this.subscribed = true;
	}

	public TransmitNetwork.RotateContext getRotate() {
		if (this.transmit != null) {
			return this.transmit.getNetwork().getRotateContext(this.worldPosition);
		}
		return TransmitNetwork.RotateContext.NULL;
	}
}
