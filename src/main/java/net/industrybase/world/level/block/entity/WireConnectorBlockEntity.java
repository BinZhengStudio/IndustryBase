package net.industrybase.world.level.block.entity;

import net.industrybase.api.electric.ElectricPower;
import net.industrybase.api.electric.IWireConnectable;
import net.industrybase.world.level.block.DynamoBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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

	@Override
	public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
		if (side == this.getBlockState().getValue(DynamoBlock.FACING)) {
			return this.electricPower.cast(cap, super.getCapability(cap, side));
		}
		return super.getCapability(cap, side);
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
	public void setRemoved() {
		this.electricPower.remove();
		super.setRemoved();
	}

	@Override
	public AABB getRenderBoundingBox() {
//		return INFINITE_EXTENT_AABB;
		BlockPos pos = this.getBlockPos();
		return new AABB(pos.offset(-256, -256, -256), pos.offset(256, 256, 256));
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
