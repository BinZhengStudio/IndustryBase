package cn.bzgzs.largeprojects.world.level.block.entity;

import cn.bzgzs.largeprojects.api.CapabilityList;
import cn.bzgzs.largeprojects.api.energy.IMechanicalTransmit;
import cn.bzgzs.largeprojects.api.energy.TransmitNetwork;
import cn.bzgzs.largeprojects.api.world.level.block.entity.BaseTransmitBlockEntity;
import cn.bzgzs.largeprojects.world.level.block.DynamoBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.IEnergyStorage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class DynamoBlockEntity extends BaseTransmitBlockEntity {
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
			return Integer.MAX_VALUE; // TODO
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

	private final LazyOptional<IMechanicalTransmit> transmit = LazyOptional.of(() -> new IMechanicalTransmit() {
		private final TransmitNetwork network = TransmitNetwork.Factory.get(DynamoBlockEntity.this.level);

		@Override
		public int getPower() {
			return 0;
		}

		@Override
		public int getResistance() {
			return 1;
		}

		@Override
		public double getSpeed() {
			return this.network.speed(DynamoBlockEntity.this.worldPosition);
		}

		@Override
		public boolean canExtract() {
			return false;
		}

		@Override
		public boolean canReceive() {
			return true;
		}
	});

	public DynamoBlockEntity(BlockPos pos, BlockState state) {
		super(BlockEntityTypeList.DYNAMO.get(), pos, state);
	}

	public static void serverTick(Level level, BlockPos pos, BlockState state, DynamoBlockEntity blockEntity) {
	}

		@Override
	public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
		if (side == this.getBlockState().getValue(DynamoBlock.FACING)) {
			return cap == CapabilityList.MECHANICAL_TRANSMIT ? this.transmit.cast() : super.getCapability(cap, side);
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
