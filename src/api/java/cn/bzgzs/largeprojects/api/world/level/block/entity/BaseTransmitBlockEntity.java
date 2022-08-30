package cn.bzgzs.largeprojects.api.world.level.block.entity;

import cn.bzgzs.largeprojects.api.energy.TransmitNetwork;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public abstract class BaseTransmitBlockEntity extends BlockEntity {
	public BaseTransmitBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
	}

	@Override
	public void onLoad() {
		if (this.level != null && !this.level.isClientSide) {
			TransmitNetwork.Factory.get(this.level).addOrChangeBlock(this.worldPosition, this::setChanged);
		}
		super.onLoad();
	}

	@Override
	public void onChunkUnloaded() {
		if (this.level != null && !this.level.isClientSide) {
			TransmitNetwork.Factory.get(this.level).removeBlock(this.worldPosition, this::setChanged);
		}
		super.onChunkUnloaded();
	}

	@Override
	public void setRemoved() {
		if (this.level != null && !this.level.isClientSide) {
			TransmitNetwork.Factory.get(this.level).removeBlock(this.worldPosition, this::setChanged);
		}
		super.setRemoved();
	}
}
