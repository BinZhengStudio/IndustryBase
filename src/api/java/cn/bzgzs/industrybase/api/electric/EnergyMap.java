package cn.bzgzs.industrybase.api.electric;

import net.minecraft.core.BlockPos;

import java.util.HashMap;
import java.util.Objects;

public class EnergyMap {
	private static final Energy ZERO = new Energy();
	private final HashMap<BlockPos, EnergyMap.Energy> map;

	public EnergyMap() {
		super();
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
		energy.output += (diff > 0 ? diff : Math.max(diff, -energy.output)); // TODO
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
		energy.input += (diff > 0 ? diff : Math.max(diff, -energy.input)); // TODO
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

	public static class Energy {
		protected long output;
		protected long input;

		public Energy() {
			this.output = 0;
			this.input = 0;
		}

		private Energy(Energy energy) {
			this.output = energy.output;
			this.input = energy.input;
		}

		private Energy(long output, long input) {
			this.output = output;
			this.input = input;
		}

		public double getOutput() {
			return output / 100.0D;
		}

		public long getOutputLong() {
			return output;
		}

		public double getInput() {
			return input / 100.0D;
		}

		public long getInputLong() {
			return input;
		}

		protected Energy add(Energy energy) {
			this.output += energy.output;
			this.input += energy.input;
			return this;
		}

		public static Energy union(Energy a, Energy b) {
			return new Energy(a.output + b.output, a.input + b.input);
		}

		protected Energy shrink(Energy energy) {
			this.output -= Math.min(energy.output, this.output);
			this.input -= Math.min(energy.input, this.input);
			return this;
		}

		public boolean isZero() {
			return this.output <= 0 && this.input <= 0;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || this.getClass() != o.getClass()) return false;
			Energy energy = (Energy) o;
			return this.output == energy.output && this.input == energy.input;
		}

		@Override
		public int hashCode() {
			return Objects.hash(this.output, this.input);
		}
	}

	public static class TempEnergy extends Energy {
		@Override
		public Energy add(Energy energy) {
			return super.add(energy);
		}

		@Override
		public Energy shrink(Energy energy) {
			return super.shrink(energy);
		}
	}
}
