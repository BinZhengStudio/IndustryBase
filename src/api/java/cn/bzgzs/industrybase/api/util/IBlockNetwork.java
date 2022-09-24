package cn.bzgzs.industrybase.api.util;

import net.minecraft.core.BlockPos;

public interface IBlockNetwork {
	int size(BlockPos node);

	BlockPos root(BlockPos node);

	@FunctionalInterface
	interface ConnectivityListener {
		void onChange(BlockPos primaryRoot, BlockPos secondaryRoot);
	}
}
