package net.industrybase.world.level.block.entity;

import net.industrybase.api.electric.ElectricPower;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class WireBlockEntity extends BlockEntity {
	private final ElectricPower electricPower = new ElectricPower(this);

	public WireBlockEntity(BlockPos pos, BlockState blockState) {
		super(BlockEntityTypeList.WIRE.get(), pos, blockState);
	}

	@Override
	public void onLoad() {
		super.onLoad();
		this.electricPower.register();
	}

	@Override
	public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
		return this.electricPower.cast(cap, super.getCapability(cap, side));
	}

	@Override
	public void setRemoved() {
		this.electricPower.remove();
		super.setRemoved();
	}
}
