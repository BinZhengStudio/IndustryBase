package net.industrybase.api.util;

import net.industrybase.api.electric.ElectricNetwork;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

public class ElectricHelper {
	public static double fromTransmit(double speed, int resistance) {
		return speed * resistance * Math.PI / 50.0D;
	}

	/**
	 * 本方法应在 {@link BlockBehaviour#onRemove} 中调用。
	 *
	 * @param level      level
	 * @param state      old state
	 * @param newState   new state
	 * @param pos        BlockPos
	 */
	@SuppressWarnings("deprecation")
	public static void updateOnRemove(LevelAccessor level, BlockState state, BlockState newState, BlockPos pos) {
		if (!level.isClientSide()) {
			if (state.is(newState.getBlock())) { // 确保是同种方块
				ElectricNetwork.Manager.get(level).addOrChangeBlock(pos, () -> {
				});
			}
		}
	}
}
