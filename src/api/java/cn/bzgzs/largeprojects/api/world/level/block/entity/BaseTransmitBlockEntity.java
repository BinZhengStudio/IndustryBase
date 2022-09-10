package cn.bzgzs.largeprojects.api.world.level.block.entity;

import cn.bzgzs.largeprojects.api.energy.TransmitNetwork;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

public abstract class BaseTransmitBlockEntity extends BlockEntity {
	private TransmitNetwork network;

	public BaseTransmitBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
		this.network = TransmitNetwork.Factory.get(this.level);
	}

	public TransmitNetwork getNetwork() {
		return this.network;
	}

	@Override
	public void setLevel(@NotNull Level level) {
		super.setLevel(level);
		this.network = TransmitNetwork.Factory.get(this.level);
	}

	@Override
	public void onLoad() {
		if (this.level != null && !this.level.isClientSide) {
			network.addOrChangeBlock(this.worldPosition, this::setChanged);
		}
		super.onLoad();
	}

	@Override
	public void onChunkUnloaded() {
		if (this.level != null && !this.level.isClientSide) {
			network.removeBlock(this.worldPosition, this::setChanged);
		}
		super.onChunkUnloaded();
	}

	@Override
	public void setRemoved() {
		if (this.level != null && !this.level.isClientSide) {
			network.removeBlock(this.worldPosition, this::setChanged);
		}
		super.setRemoved();
	}
}
