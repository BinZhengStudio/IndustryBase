package cn.bzgzs.industrybase.world.level.block.entity;

import cn.bzgzs.industrybase.api.CapabilityList;
import cn.bzgzs.industrybase.api.electric.ElectricPower;
import cn.bzgzs.industrybase.api.electric.IWireConnectable;
import cn.bzgzs.industrybase.world.level.block.DynamoBlock;
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

public class WireConnectorBlockEntity extends BlockEntity implements IWireConnectable {
	private final ElectricPower electricPower = new ElectricPower(this);

	public WireConnectorBlockEntity(BlockPos pos, BlockState state) {
		super(BlockEntityTypeList.WIRE_CONNECTOR.get(), pos, state);
	}

	@Override
	public void onLoad() {
		super.onLoad();
		this.electricPower.registerToNetwork();
	}

	@Override
	public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
		if (side == this.getBlockState().getValue(DynamoBlock.FACING)) {
			return cap == CapabilityList.ELECTRIC_POWER ? this.electricPower.cast() : super.getCapability(cap, side);
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
	public void onChunkUnloaded() {
		this.electricPower.removeFromNetwork();
		super.onChunkUnloaded();
	}

	@Override
	public void setRemoved() {
		this.electricPower.removeFromNetwork();
		super.setRemoved();
	}

	@Override
	public AABB getRenderBoundingBox() {
//		return INFINITE_EXTENT_AABB;
		BlockPos pos = this.getBlockPos();
		return new AABB(pos.offset(-256, -256, -256), pos.offset(256, 256, 256));
//		return super.getRenderBoundingBox();
	}
}
