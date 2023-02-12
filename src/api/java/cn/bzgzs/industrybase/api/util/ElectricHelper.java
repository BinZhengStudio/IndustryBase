package cn.bzgzs.industrybase.api.util;

import cn.bzgzs.industrybase.api.electric.ElectricNetwork;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;

public class ElectricHelper {
	public static double fromTransmit(double speed, int resistance) {
		return speed * resistance * Math.PI / 50.0D;
	}

	/**
	 * 本方法应在 {@link BlockBehaviour#onRemove} 中调用。
	 * @param level level
	 * @param state old state
	 * @param newState new state
	 * @param pos BlockPos
	 * @param properties 需要比较的 Property
	 */
	@SuppressWarnings("deprecation")
	public static void updateOnRemove(LevelAccessor level, BlockState state, BlockState newState, BlockPos pos, Property<?>... properties) {
		if (state.is(newState.getBlock())) { // 确保是同种方块
			for (Property<?> property : properties) { // 若新方块与旧方块相关的 state 有不相等的情况，则更新能量网络
				if (state.getValue(property) != newState.getValue(property)) {
					ElectricNetwork.Manager.get(level).addOrChangeBlock(pos, () -> {
					});
					return;
				}
			}
		}
	}
}
