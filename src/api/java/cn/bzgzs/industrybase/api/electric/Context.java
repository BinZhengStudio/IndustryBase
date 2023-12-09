package cn.bzgzs.industrybase.api.electric;

public class Context extends Energy implements IContext {
	private final int hashCode;
	private int size;
	private int forgeEnergy = 0;
	private int FEInput = 0;

//	protected Context(int hashCode) {
//		super();
//		this.hashCode = hashCode;
//	}

	protected Context(int hashCode, int size) {
		super();
		this.hashCode = hashCode;
		this.size = size;
	}

	protected Context(int hashCode, int size, Energy energy) {
		super(energy);
		this.hashCode = hashCode;
		this.size = size;
	}

//	protected Context(int hashCode, int size, Energy energy1, Energy energy2) {
//		super(energy1.output + energy2.output, energy1.input + energy2.input);
//		this.hashCode = hashCode;
//		this.size = size;
//	}

//	protected Context(int hashCode, int size, Energy... energies) {
//		super();
//		for (Energy energy : energies) {
//			this.output += energy.output;
//			this.input += energy.input;
//		}
//		this.hashCode = hashCode;
//		this.size = size;
//	}

//	protected Context(int hashCode, Context context) {
//		super(context);
//		this.hashCode = hashCode;
//		this.size = context.size;
//		this.forgeEnergy = context.forgeEnergy;
//		this.FEInput = context.FEInput;
//	}

	protected Context(int hashCode, int size, long output, long input) {
		super(output, input);
		this.hashCode = hashCode;
		this.size = size;
	}

//	protected Context(int hashCode, int size, long output, long input, int forgeEnergy) {
//		this(hashCode, size, output, input);
//		this.forgeEnergy = forgeEnergy;
//	}

//	protected Context(int hashCode, int size, long output, long input, int forgeEnergy, int FEInput) {
//		this(hashCode, size, output, input);
//		this.forgeEnergy = forgeEnergy;
//		this.FEInput = FEInput;
//	}

//	protected void setForgeEnergy(int energy) {
//		this.forgeEnergy = Math.max(energy, 0);
//	}

	protected void clearFEInput() {
		this.FEInput = 0;
	}

	protected void convertFEInput(int RequiredFE) {
		int converted = Math.min(RequiredFE, this.forgeEnergy);
		this.FEInput = converted;
		this.forgeEnergy -= converted;
	}

	protected Context addEnergy(Energy energy) {
		super.add(energy);
		return this;
	}

	protected void addForgeEnergy(int diff) {
		this.forgeEnergy -= Math.min(-diff, this.forgeEnergy);
	}

//	protected void addFEInput(int diff) {
//		this.FEInput -= Math.min(-diff, this.FEInput);
//	}

	protected void addAll(Context context) {
		this.size += context.size;
		this.output += context.output;
		this.input += context.input;
		this.forgeEnergy += context.forgeEnergy;
		this.FEInput += context.FEInput;
	}

	protected void addSize() {
		this.size++;
	}

//	protected void addSize(int amount) {
//		this.size -= Math.min(-amount, this.size);
//	}

	protected int getForgeEnergy() {
		return this.forgeEnergy;
	}

	protected boolean hasForgeEnergy() {
		return this.forgeEnergy > 0;
	}

	protected int getFEInput() {
		return this.FEInput;
	}

	protected int size() {
		return this.size;
	}

	protected Context shrink(Context context) {
		this.size -= Math.min(context.size, this.size);
		return this.shrinkEnergy(context);
	}

	protected Context shrinkEnergy(Energy energy) {
		super.shrink(energy);
		return this;
	}

//	protected Context shrinkAll(Context context) {
//		this.size -= Math.min(context.size, this.size);
//		this.forgeEnergy -= Math.min(context.forgeEnergy, this.forgeEnergy);
//		this.FEInput -= Math.min(context.FEInput, this.FEInput);
//		return this.shrinkEnergy(context);
//	}

//	protected static Context union(Context a, Context b) {
//		return new Context(Objects.hash(a, b),
//				a.size + b.size,
//				a.output + b.output,
//				a.input + b.input,
//				a.forgeEnergy + b.forgeEnergy,
//				a.FEInput + b.FEInput
//		);
//	}

	@Override
	public Context getLastContext() {
		return this;
	}

	@Override
	public ContextWrapper getLastWrapper() {
		return null;
	}

//	protected boolean valueEquals(@Nullable Object o) {
//		if (this == o) return true;
//		if (o == null || getClass() != o.getClass()) return false;
//		if (!super.equals(o)) return false;
//		Context context = (Context) o;
//		return size == context.size && forgeEnergy == context.forgeEnergy && FEInput == context.FEInput;
//	}

	@Override
	public int hashCode() {
		return this.hashCode;
	}
}
