package cn.bzgzs.industrybase.api.transmit;

public abstract class LayeredTransmissionRodBlock extends TransmissionRodBlock {
	private final int red;
	private final int green;
	private final int blue;

	public LayeredTransmissionRodBlock(Properties properties, int maxResistance, int red, int green, int blue) {
		super(properties, maxResistance);
		this.red = red;
		this.green = green;
		this.blue = blue;
	}

	public float getRed() {
		return red / 255.0F;
	}

	public float getGreen() {
		return green / 255.0F;
	}

	public float getBlue() {
		return blue / 255.0F;
	}
}
