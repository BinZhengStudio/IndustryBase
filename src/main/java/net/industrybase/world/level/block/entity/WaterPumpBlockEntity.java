package net.industrybase.world.level.block.entity;

import net.industrybase.api.pipe.PipeConnectedHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class WaterPumpBlockEntity extends BlockEntity {
	private final PipeConnectedHandler handler = new PipeConnectedHandler();

	public WaterPumpBlockEntity(BlockPos pos, BlockState state) {
		super(BlockEntityTypeList.WATER_PUMP.get(), pos, state);
	}

	@Override
	public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
		return this.handler.cast(cap, super.getCapability(cap, side));
	}
}
