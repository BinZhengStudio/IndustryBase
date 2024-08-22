package net.industrybase.api.pipe;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.Nullable;

public class PipeConnectedHandler {
	@Nullable
	private Level level;
	private final BlockPos pos;
	private final BlockEntity blockEntity;
	@Nullable
	private PipeNetwork network;

	public PipeConnectedHandler(BlockEntity blockEntity) {
		this.blockEntity = blockEntity;
		this.pos = blockEntity.getBlockPos();
	}

	public void registerHandler(StorageInterface storageInterface) {
		this.level = this.blockEntity.getLevel();
		if (this.level != null) {
			this.network = PipeNetwork.Manager.get(this.level);
			if (!this.level.isClientSide) {
				this.network.registerHandler(this.pos, storageInterface, this.blockEntity::setChanged);
			}
		}
	}

	public void removeHandler() {
		if (this.network != null) {
			this.network.removePipe(this.pos, this.blockEntity::setChanged);
		}
	}

	public void registerPipe() {
		// TODO
	}

	public void setPressure(Direction direction, double pressure) {
		if (this.network != null) {
			this.network.setPressure(this.pos, direction, pressure);
		}
	}
}
