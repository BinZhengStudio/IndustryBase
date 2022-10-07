package cn.bzgzs.industrybase.api.electric;

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelAccessor;

public class ConnectHelper {
	@CanIgnoreReturnValue
	public static boolean addConnect(LevelAccessor level, BlockPos from, BlockPos to, Runnable callback) {
		ElectricNetwork network = ElectricNetwork.Manager.get(level);
		return network.addWire(from, to, callback);
	}
}
