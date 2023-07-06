package cn.bzgzs.industrybase.api.electric;

import net.minecraft.core.BlockPos;

import java.util.Set;

public interface IWireConnectable {
	boolean isSubscribed();

	void setSubscribed();

	Set<BlockPos> getWires();
}
