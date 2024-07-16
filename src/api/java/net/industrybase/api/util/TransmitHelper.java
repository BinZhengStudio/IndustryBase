package net.industrybase.api.util;

import net.industrybase.api.transmit.TransmitNetwork;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

public class TransmitHelper {
	public static int fromElectric(double electricPower) {
		return (int) (electricPower * 50 / Math.PI);
	}

	/**
	 * 本方法应在 {@link BlockBehaviour#onRemove} 中调用。
	 *
	 * @param level    level
	 * @param state    old state
	 * @param newState new state
	 * @param pos      BlockPos
	 */
	@SuppressWarnings("deprecation")
	public static boolean updateOnRemove(LevelAccessor level, BlockState state, BlockState newState, BlockPos pos) {
		if (!level.isClientSide()) {
			if (state.is(newState.getBlock())) { // 确保是同种方块，防止重复更新
				TransmitNetwork.Manager.get(level).addOrChangeBlock(pos, () -> {
				});
				return true;
			}
		}
		return false;
	}
}
