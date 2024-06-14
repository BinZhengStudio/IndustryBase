package net.industrybase.world.level.block.entity;

import net.industrybase.api.CapabilityList;
import net.industrybase.api.transmit.MechanicalTransmit;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class AxisConnectorBlockEntity extends BlockEntity {
	private final MechanicalTransmit transmit = new MechanicalTransmit(this);

	public AxisConnectorBlockEntity(BlockPos pos, BlockState state) {
		super(BlockEntityTypeList.GEAR_BOX.get(), pos, state);
	}

	@Override
	public void onLoad() {
		super.onLoad();
		this.transmit.register();
	}

	@Override
	public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
		return cap == CapabilityList.MECHANICAL_TRANSMIT ? this.transmit.cast() : super.getCapability(cap, side);
	}

	@Override
	public void setRemoved() {
		this.transmit.remove();
		super.setRemoved();
	}
}
