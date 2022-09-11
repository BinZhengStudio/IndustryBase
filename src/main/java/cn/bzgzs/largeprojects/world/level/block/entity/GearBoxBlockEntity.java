package cn.bzgzs.largeprojects.world.level.block.entity;

import cn.bzgzs.largeprojects.api.CapabilityList;
import cn.bzgzs.largeprojects.api.energy.IMechanicalTransmit;
import cn.bzgzs.largeprojects.api.world.level.block.entity.BaseTransmitBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class GearBoxBlockEntity extends BaseTransmitBlockEntity {
	private final LazyOptional<IMechanicalTransmit> transmit = LazyOptional.of(() -> new IMechanicalTransmit() {
		@Override
		public int getPower() {
			return 0;
		}

		@Override
		public int getResistance() {
			return 0;
		}

		@Override
		public double getSpeed() {
			return 0;
		}

		@Override
		public boolean canExtract() {
			return false;
		}

		@Override
		public boolean canReceive() {
			return false;
		}
	});

	public GearBoxBlockEntity(BlockPos pos, BlockState state) {
		super(BlockEntityTypeList.GEAR_BOX.get(), pos, state);
	}

	@Override
	public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
		return cap == CapabilityList.MECHANICAL_TRANSMIT ? this.transmit.cast() : super.getCapability(cap);
	}
}
