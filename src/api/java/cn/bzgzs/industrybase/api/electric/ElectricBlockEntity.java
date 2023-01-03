package cn.bzgzs.industrybase.api.electric;

import cn.bzgzs.industrybase.api.CapabilityList;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("unused")
public class ElectricBlockEntity extends BlockEntity {
	private final ElectricPower electricPower = new ElectricPower(this);

	public ElectricBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState blockState) {
		super(type, pos, blockState);
	}

	@Override
	public void onLoad() {
		super.onLoad();
		this.electricPower.registerToNetwork();
	}

	@Override
	public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
		if (cap == CapabilityList.ELECTRIC_POWER) {
			return this.electricPower.cast();
		}
		return super.getCapability(cap, side);
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
	public void load(CompoundTag tag) {
		super.load(tag);
		this.electricPower.readFromNBT(tag);
	}

	@Override
	protected void saveAdditional(CompoundTag tag) {
		super.saveAdditional(tag);
		this.electricPower.writeToNBT(tag);
	}
}
