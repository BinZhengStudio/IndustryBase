package cn.bzgzs.industrybase.api.electric;

import net.minecraft.core.BlockPos;

import javax.annotation.Nullable;
import java.util.HashMap;

public class ComponentMap extends HashMap<BlockPos, ContextWrapper> {
	private final EnergyMap energyMap;

	public ComponentMap(EnergyMap energyMap) {
		super();
		this.energyMap = energyMap;
	}

	public ContextWrapper get(BlockPos k) {
		return this.computeIfAbsent(k, pos -> new ContextWrapper(1, pos.hashCode(), 1, this.energyMap.get(k)));
	}

	@Override
	public ContextWrapper put(BlockPos key, ContextWrapper value) {
		return super.put(key, value);
	}

	@Nullable
	public ContextWrapper getNullable(BlockPos k) {
		return super.get(k);
	}
}
