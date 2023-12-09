package cn.bzgzs.industrybase.api.electric;

import javax.annotation.Nonnull;

public class ContextWrapper implements IContext {
	private int layer;
	private IContext context;

//	public ContextWrapper(int layer, int hashCode, int size) {
//		this(layer, new Context(hashCode, size));
//	}

	public ContextWrapper(int layer, int hashCode, int size, Energy energy) {
		this(layer, new Context(hashCode, size, energy));
	}

	public ContextWrapper(int layer, int hashCode, int size, Energy energy1, Energy energy2) {
		this(layer, new Context(hashCode, size, energy1.output + energy2.output, energy1.input + energy2.input));
	}

	public ContextWrapper(int layer, IContext context) {
		this.layer = layer;
		this.context = context;
	}

	public void setContext(IContext context) {
		this.context = context;
	}

	public Context getContext() {
		return this.getLast().getLastContext();
	}

	public int layer() {
		return this.layer;
	}

	public void setLayer(int layer) {
		if (layer > this.layer) this.layer = layer;
	}

	@Override
	public Context getLastContext() {
		return this.context.getLastContext();
	}

	public ContextWrapper getLast() {
		ContextWrapper wrapper = this.getLastWrapper();
		if (wrapper != this && wrapper != this.context) this.context = wrapper;
		return wrapper;
	}

	@Nonnull
	@Override
	public ContextWrapper getLastWrapper() {
		ContextWrapper wrapper = this.context.getLastWrapper();
		if (wrapper == null) return this;
		return wrapper;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		ContextWrapper that = (ContextWrapper) o;
		return this.context.getLastContext() == that.context.getLastContext();
	}
}
