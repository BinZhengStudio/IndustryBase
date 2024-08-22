package net.industrybase.world.level.block.entity;

import net.industrybase.api.pipe.PipeNetwork;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class IronPipeBlockEntity extends BlockEntity {
	private PipeNetwork network;

	public IronPipeBlockEntity(BlockPos pos, BlockState state) {
		super(BlockEntityTypeList.IRON_PIPE.get(), pos, state);
	}

	@Override
	public void onLoad() {
		super.onLoad();
		if (this.level != null) {
			this.network = PipeNetwork.Manager.get(this.level);
			this.network.registerPipe(this.worldPosition, this::setChanged);
		}
	}

	@Override
	public void setRemoved() {
		if (this.network != null) {
			this.network.removePipe(this.worldPosition, this::setChanged);
		}
		super.setRemoved();
	}
}
