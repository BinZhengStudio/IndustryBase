package cn.bzgzs.industrybase.world.level.block.entity;

import cn.bzgzs.industrybase.api.CapabilityList;
import cn.bzgzs.industrybase.api.world.level.block.entity.BaseTransmitBlockEntity;
import cn.bzgzs.industrybase.world.level.block.TransmissionRodBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TransmissionRodBlockEntity extends BaseTransmitBlockEntity {

	public TransmissionRodBlockEntity(BlockPos pos, BlockState state) {
		super(BlockEntityTypeList.TRANSMISSION_ROD.get(), pos, state);
	}

	@Override
	public boolean canExtract() { // TODO
		return false;
	}

	@Override
	public boolean canReceive() { // TODO
		return false;
	}

	@Override
	public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
		if (side != null && side.getAxis() == this.getBlockState().getValue(TransmissionRodBlock.AXIS)) {
			return cap == CapabilityList.MECHANICAL_TRANSMIT ? this.getTransmit().cast() : super.getCapability(cap, side);
		}
		return super.getCapability(cap, side);
	}
}
