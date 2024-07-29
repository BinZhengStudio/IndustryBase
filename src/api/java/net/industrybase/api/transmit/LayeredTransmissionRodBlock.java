package net.industrybase.api.transmit;

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

	public int getRed() {
		return this.red;
	}

	public int getGreen() {
		return this.green;
	}

	public int getBlue() {
		return this.blue;
	}
}
