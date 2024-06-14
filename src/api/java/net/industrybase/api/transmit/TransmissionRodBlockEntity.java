package net.industrybase.api.transmit;

import net.industrybase.api.CapabilityList;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TransmissionRodBlockEntity extends BlockEntity {
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

	@Override
	public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
		if (side != null && side.getAxis() == this.getBlockState().getValue(TransmissionRodBlock.AXIS)) {
			return cap == CapabilityList.MECHANICAL_TRANSMIT ? this.transmit.cast() : super.getCapability(cap, side);
		}
		return super.getCapability(cap, side);
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
		if (this.transmit.getNetwork() != null) {
			return this.transmit.getNetwork().getRotateContext(this.worldPosition);
		}
		return TransmitNetwork.RotateContext.NULL;
	}
}
