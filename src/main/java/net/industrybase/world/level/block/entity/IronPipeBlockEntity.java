package net.industrybase.world.level.block.entity;

import net.industrybase.api.pipe.PipeNetwork;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

public class IronPipeBlockEntity extends BlockEntity {
	private static final AABB AABB = new AABB(0.3125D, 0.3125D, 0.3125D, 0.6875D, 0.6875D, 0.6875D);
	private PipeNetwork network;

	public IronPipeBlockEntity(BlockPos pos, BlockState state) {
		super(BlockEntityTypeList.IRON_PIPE.get(), pos, state);
	}

	@Override
	public void onLoad() {
		super.onLoad();
		if (this.level != null) {
			this.network = PipeNetwork.Manager.get(this.level);
			this.network.registerPipe(this.worldPosition, AABB, this::setChanged);
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
