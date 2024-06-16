package net.industrybase.api.transmit;

import net.minecraft.world.level.block.state.BlockBehaviour;

public abstract class WoodTransmissionRod extends TransmissionRodBlock {
	public WoodTransmissionRod(Properties properties) {
		super(properties, 5);
	}

	public WoodTransmissionRod(BlockBehaviour wood) {
		super(Properties.ofFullCopy(wood), 5);
	}

	public WoodTransmissionRod(Properties properties, int maxResistance) {
		super(properties, maxResistance);
	}
}
