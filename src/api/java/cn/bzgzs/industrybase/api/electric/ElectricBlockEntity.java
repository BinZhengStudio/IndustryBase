package cn.bzgzs.industrybase.api.electric;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class ElectricBlockEntity extends BlockEntity {
	private final ElectricPower electricPower = new ElectricPower(this);

	public ElectricBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState blockState) {
		super(type, pos, blockState);
	}

	@Override
	public void load(CompoundTag tag) {
		super.load(tag);
		this.electricPower.readFromNBT(tag);
	}

	@Override
	protected void saveAdditional(CompoundTag tag) {
		super.saveAdditional(tag);
		this.electricPower.writeToNBT(tag);
	}

	@Override
	public void onLoad() {
		this.electricPower.registerToNetwork();
		super.onLoad();
	}

	@Override
	public void onChunkUnloaded() {
		this.electricPower.removeFromNetwork();
		super.onChunkUnloaded();
	}

	@Override
	public void setRemoved() {
		this.electricPower.removeFromNetwork();
		super.setRemoved();
	}
}
