package cn.bzgzs.industrybase.api.electric;

import net.minecraft.core.BlockPos;

import java.util.HashMap;

public class EnergyMap {
	private static final Energy ZERO = new Energy();
	private final HashMap<BlockPos, Energy> map;

	public EnergyMap() {
		this.map = new HashMap<>();
	}

	public void add(BlockPos k, Energy diff) {
		if (diff.isZero()) return;
		Energy energy;
		if ((energy = this.map.get(k)) == null) {
			this.map.put(k, new Energy(diff));
			return;
		}
		energy.add(diff);
	}

	public void addOutput(BlockPos k, long diff) {
		if (diff == 0L) return;
		Energy energy;
		if ((energy = this.map.get(k)) == null) {
			if (diff < 0L) return;
			this.map.put(k, new Energy(diff, 0));
			return;
		}
		energy.addOutput(diff);
		if (energy.isZero()) this.map.remove(k);
	}

	public void addInput(BlockPos k, long diff) {
		if (diff == 0L) return;
		Energy energy;
		if ((energy = this.map.get(k)) == null) {
			if (diff < 0L) return;
			this.map.put(k, new Energy(0, diff));
			return;
		}
		energy.addInput(diff);
		if (energy.isZero()) this.map.remove(k);
	}

	public void shrink(BlockPos k, Energy diff) {
		Energy energy;
		if ((energy = this.map.get(k)) != null) {
			if (energy.shrink(diff).isZero()) {
				this.map.remove(k);
			}
		}
	}

	public Energy get(BlockPos k) {
		return this.map.getOrDefault(k, ZERO);
	}

	public void put(BlockPos pos, Energy energy) {
		if (!energy.isZero()) {
			this.map.put(pos, energy);
		}
	}

	public Energy remove(BlockPos k) {
		Energy energy;
		return (energy = this.map.remove(k)) == null ? ZERO : energy;
	}
}
