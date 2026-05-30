package net.industrybase.world.level.block;

import org.jspecify.annotations.Nullable;

import net.minecraft.resources.Identifier;

public abstract class LayeredTransmissionRodBlock extends TransmissionRodBlock {
	private final int rgbColor;

	public LayeredTransmissionRodBlock(Properties properties, int maxResistance, int rgbColor) {
		super(properties, maxResistance);
		this.rgbColor = rgbColor;
	}

    @Nullable
    @Override
    public Identifier getTexture() {
        return null;
    }

	public int getRgbColor() {
		return this.rgbColor;
	}
}
