package cn.bzgzs.industrybase.world.level.block.entity;

import cn.bzgzs.industrybase.api.CapabilityList;
import cn.bzgzs.industrybase.api.world.level.block.entity.BaseTransmitBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class GearBoxBlockEntity extends BaseTransmitBlockEntity {
	public GearBoxBlockEntity(BlockPos pos, BlockState state) {
		super(BlockEntityTypeList.GEAR_BOX.get(), pos, state);
	}

	@Override
	public boolean canExtract() {
		return false;
	}

	@Override
	public boolean canReceive() {
		return false;
	}

	@Override
	public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
		return cap == CapabilityList.MECHANICAL_TRANSMIT ? this.getTransmit().cast() : super.getCapability(cap);
	}
}
