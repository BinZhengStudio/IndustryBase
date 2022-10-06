package cn.bzgzs.industrybase.api.world.level.block.entity;

import cn.bzgzs.industrybase.api.transmit.MechanicalTransmit;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

@SuppressWarnings("unused")
public class TransmitBlockEntity extends BlockEntity {
	private final MechanicalTransmit transmit = new MechanicalTransmit(this);

	public TransmitBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
	}

	@Override
	public void load(CompoundTag tag) {
		super.load(tag);
		this.transmit.readFromNBT(tag);
	}

	@Override
	protected void saveAdditional(CompoundTag tag) {
		super.saveAdditional(tag);
		this.transmit.writeToNBT(tag);
	}

	@Override
	public void onLoad() {
		this.transmit.registerToNetwork();
		super.onLoad();
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
