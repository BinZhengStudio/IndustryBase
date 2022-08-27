package cn.bzgzs.largeprojects.api.util;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

public interface IBlockNetwork {
	int size(BlockPos node);

	BlockPos root(BlockPos node);

	void cut(BlockPos nodePos, Direction direction, ConnectivityListener afterSplit);

	void link(BlockPos nodePos, Direction direction, ConnectivityListener beforeMerge);

	@FunctionalInterface
	interface ConnectivityListener {
		void onChange(BlockPos primaryNode, BlockPos secondaryNode);
	}
}
