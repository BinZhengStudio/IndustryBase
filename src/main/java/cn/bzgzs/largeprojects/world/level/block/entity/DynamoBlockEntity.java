package cn.bzgzs.largeprojects.world.level.block.entity;

import cn.bzgzs.largeprojects.world.level.block.DynamoBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.IEnergyStorage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class DynamoBlockEntity extends BlockEntity {
	private double storageFE;
	private final LazyOptional<IEnergyStorage> forgeEnergy = LazyOptional.of(() -> new IEnergyStorage() {
		@Override
		public int receiveEnergy(int maxReceive, boolean simulate) {
			return 0;
		}

		@Override
		public int extractEnergy(int maxExtract, boolean simulate) {
			return 0;
		}

		@Override
		public int getEnergyStored() {
			return 0;
		}

		@Override
		public int getMaxEnergyStored() {
			return 0;
		}

		@Override
		public boolean canExtract() {
			return true;
		}

		@Override
		public boolean canReceive() {
			return false;
		}
	});

	public DynamoBlockEntity(BlockPos pos, BlockState state) {
		super(BlockEntityTypeList.DYNAMO.get(), pos, state);
	}

	@Override
	public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
		if (side == this.getBlockState().getValue(DynamoBlock.FACING)) {
			return super.getCapability(cap, side); // TODO
		} else {
			return cap == ForgeCapabilities.ENERGY ? this.forgeEnergy.cast() : super.getCapability(cap, side);
		}
	}

	@Override
	public void load(CompoundTag tag) {
		super.load(tag);
		if (tag.contains("StorageFE")) this.storageFE = tag.getDouble("StorageFE");
	}

	@Override
	protected void saveAdditional(CompoundTag tag) {
		super.saveAdditional(tag);
		tag.putDouble("StorageFE", this.storageFE);
	}
}
