package cn.bzgzs.industrybase.api.transmit;

import net.minecraft.world.level.block.state.BlockBehaviour;

public abstract class WoodTransmissionRod extends TransmissionRodBlock {
	public WoodTransmissionRod(Properties properties) {
		super(properties, 5);
	}

	public WoodTransmissionRod(BlockBehaviour fromWood) {
		super(Properties.copy(fromWood), 5);
	}

	public WoodTransmissionRod(Properties properties, int maxResistance) {
		super(properties, maxResistance);
	}
}
