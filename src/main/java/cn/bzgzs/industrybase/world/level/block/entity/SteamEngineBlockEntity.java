package cn.bzgzs.industrybase.world.level.block.entity;

import cn.bzgzs.industrybase.api.CapabilityList;
import cn.bzgzs.industrybase.api.world.level.block.entity.BaseTransmitBlockEntity;
import cn.bzgzs.industrybase.world.level.block.SteamEngineBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SteamEngineBlockEntity extends BaseTransmitBlockEntity {

	public SteamEngineBlockEntity(BlockPos pos, BlockState state) {
		super(BlockEntityTypeList.STEAM_ENGINE.get(), pos, state);
	}

	@Override
	public void onLoad() {
		super.onLoad();
		this.setPower(1);
		this.setResistance(1);
	}

	@Override
	public boolean canExtract() {
		return true;
	}

	@Override
	public boolean canReceive() {
		return false;
	}

	@Override
	public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
		if (side != null && side.getAxis() == this.getBlockState().getValue(SteamEngineBlock.AXIS)) {
			return cap == CapabilityList.MECHANICAL_TRANSMIT ? this.getTransmit().cast() : super.getCapability(cap, side);
		}
		return super.getCapability(cap, side);
	}
}
