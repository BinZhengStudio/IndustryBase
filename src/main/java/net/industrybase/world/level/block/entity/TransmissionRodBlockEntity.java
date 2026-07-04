package net.industrybase.world.level.block.entity;

import net.industrybase.api.transmit.MechanicalTransmit;
import net.industrybase.api.transmit.TransmitNetwork;
import net.industrybase.world.level.block.CreativeDynamoBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import org.jetbrains.annotations.Nullable;

public class TransmissionRodBlockEntity extends BlockEntity {
	private final MechanicalTransmit transmit = new MechanicalTransmit(this);
	private boolean subscribed = false;

	public TransmissionRodBlockEntity(BlockPos pos, BlockState state) {
		super(BlockEntityTypeList.TRANSMISSION_ROD.get(), pos, state);
	}

	@Override
	public void onLoad() {
		super.onLoad();
		this.transmit.register();
	}

	@Nullable
	public MechanicalTransmit getTransmit(Direction side) {
		if (side != null && side.getAxis() == this.getBlockState().getValue(BlockStateProperties.AXIS)) {
			return this.transmit;
		}
		return null;
	}

	@Override
	public void setRemoved() {
		this.transmit.remove();
		super.setRemoved();
	}

    @Override
    @SuppressWarnings("deprecation")
    public void setBlockState(BlockState blockState) {
        var oldState = this.getBlockState();
        super.setBlockState(blockState);
        if (oldState.getValue(CreativeDynamoBlock.FACING) != blockState.getValue(CreativeDynamoBlock.FACING)) {
            TransmitNetwork.Manager.get(level).addOrChangeBlock(this.worldPosition, this::invalidateCapabilities);
        }
    }

	public boolean isSubscribed() {
		return this.subscribed;
	}

	public void setSubscribed() {
		this.subscribed = true;
	}

	public TransmitNetwork.RotateContext getRotate() {
		if (this.transmit.getNetwork() != null) {
			return this.transmit.getNetwork().getRotateContext(this.worldPosition);
		}
		return TransmitNetwork.RotateContext.NULL;
	}
}
