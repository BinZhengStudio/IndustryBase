package net.industrybase.world.level.block.entity;

import net.industrybase.api.electric.ElectricPower;
import net.industrybase.api.electric.IWireConnectable;
import net.industrybase.world.level.block.DynamoBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.Set;

public class WireConnectorBlockEntity extends BlockEntity implements IWireConnectable {
	private final ElectricPower electricPower = new ElectricPower(this);
	private boolean subscribed = false;

	public WireConnectorBlockEntity(BlockPos pos, BlockState state) {
		super(BlockEntityTypeList.WIRE_CONNECTOR.get(), pos, state);
	}

	@Override
	public void onLoad() {
		super.onLoad();
		this.electricPower.register();
	}

	@Nullable
	public ElectricPower getElectricPower(Direction side) {
		if (side == this.getBlockState().getValue(DynamoBlock.FACING)) {
			return this.electricPower;
		}
		return null;
	}

	@Override
	public void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
		super.loadAdditional(tag, registries);
		this.electricPower.readFromNBT(tag);
	}

	@Override
	protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
		super.saveAdditional(tag, registries);
		this.electricPower.writeToNBT(tag);
	}

	@Override
	public void setRemoved() {
		this.electricPower.remove();
		super.setRemoved();
	}

	@Override
	public boolean isSubscribed() {
		return this.subscribed;
	}

	@Override
	public void setSubscribed() {
		this.subscribed = true;
	}

	@Override
	public Set<BlockPos> getWires() {
		if (this.electricPower.getNetwork() != null) {
			return this.electricPower.getNetwork().getWireConn(this.worldPosition);
		}
		return new HashSet<>();
	}
}
