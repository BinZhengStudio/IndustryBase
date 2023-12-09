package cn.bzgzs.industrybase.api.electric;

import java.util.Objects;

public class Energy {
	protected long output;
	protected long input;

	public Energy() {
		this.output = 0L;
		this.input = 0L;
	}

	public Energy(Energy energy) {
		this.output = energy.output;
		this.input = energy.input;
	}

	public Energy(long output, long input) {
		this.output = Math.max(output, 0L);
		this.input = Math.max(input, 0L);
	}

//	protected void set(long output, long input) {
//		this.output = Math.max(output, 0L);
//		this.input = Math.max(input, 0L);
//	}

//	protected void set(Energy energy) {
//		this.output = energy.output;
//		this.input = energy.input;
//	}

//	protected void setOutput(long output) {
//		this.output = Math.max(output, 0L);
//	}

//	protected void setInput(long input) {
//		this.input = Math.max(input, 0L);
//	}

	protected void add(long output, long input) {
		this.output -= Math.min(-output, this.output);
		this.input -= Math.min(-input, this.input);
	}

	protected Energy add(Energy energy) {
		this.output += energy.output;
		this.input += energy.input;
		return this;
	}

	protected void addOutput(long output) {
		this.output -= Math.min(-output, this.output);
	}

	protected void addInput(long input) {
		this.input -= Math.min(-input, this.input);
	}

	protected Energy shrink(Energy energy) {
		this.output -= Math.min(energy.output, this.output);
		this.input -= Math.min(energy.input, this.input);
		return this;
	}

//	protected static Energy union(Energy a, Energy b) {
//		return new Energy(a.output + b.output, a.input + b.input);
//	}

	protected boolean isZero() {
		return this.output <= 0 && this.input <= 0;
	}

	protected double getOutputEnergy() {
		return this.output / 100.0D;
	}

	protected long getOutput() {
		return this.output;
	}

	protected double getInputEnergy() {
		return this.input / 100.0D;
	}

	protected long getInput() {
		return this.input;
	}

//	protected boolean valueEquals(@Nullable Object o) {
//		if (this == o) return true;
//		if (o == null || this.getClass() != o.getClass()) return false;
//		Energy energy = (Energy) o;
//		return this.output == energy.output && this.input == energy.input;
//	}

	@Override
	public int hashCode() {
		return Objects.hash(this.output, this.input);
	}
}
