package cn.bzgzs.industrybase.api.electric;

import net.minecraft.core.BlockPos;

import java.util.Collection;

public interface IWireConnectable {
	Collection<BlockPos> getConnections();
}
