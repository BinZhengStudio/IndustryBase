package cn.bzgzs.industrybase.api.util;

import cn.bzgzs.industrybase.api.transmit.TransmitNetwork;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;

public class TransmitHelper {
	public static int fromElectric(double electricPower) {
		return (int) (electricPower * 50 / Math.PI);
	}

	/**
	 * 本方法应在 {@link BlockBehaviour#onRemove} 中调用。
	 *
	 * @param level      level
	 * @param state      old state
	 * @param newState   new state
	 * @param pos        BlockPos
	 * @param properties 需要比较的 Property
	 */
	@SuppressWarnings("deprecation")
	public static void updateOnRemove(LevelAccessor level, BlockState state, BlockState newState, BlockPos pos, Property<?>... properties) {
		if (state.is(newState.getBlock())) { // 确保是同种方块，防止重复更新
			TransmitNetwork.Manager.get(level).addOrChangeBlock(pos, () -> {
			});
		}
	}
}
