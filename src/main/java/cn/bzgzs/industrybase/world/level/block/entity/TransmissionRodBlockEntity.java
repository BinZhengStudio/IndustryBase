package cn.bzgzs.industrybase.world.level.block.entity;

import cn.bzgzs.industrybase.api.CapabilityList;
import cn.bzgzs.industrybase.api.transmit.MechanicalTransmit;
import cn.bzgzs.industrybase.world.level.block.IronTransmissionRodBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class TransmissionRodBlockEntity extends BlockEntity {
	private final MechanicalTransmit transmit = new MechanicalTransmit(this);

	public TransmissionRodBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
	}

	@Override
	public void onLoad() {
		super.onLoad();
		this.transmit.registerToNetwork();
	}

	@Override
	public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
		if (side != null && side.getAxis() == this.getBlockState().getValue(IronTransmissionRodBlock.AXIS)) {
			return cap == CapabilityList.MECHANICAL_TRANSMIT ? this.transmit.cast() : super.getCapability(cap, side);
		}
		return super.getCapability(cap, side);
	}

	@Override
	public void onChunkUnloaded() {
		this.transmit.removeFromNetwork();
		super.onChunkUnloaded();
	}

	@Override
	public void setRemoved() {
		this.transmit.removeFromNetwork();
		super.setRemoved();
	}
}
