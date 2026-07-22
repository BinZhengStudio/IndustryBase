package net.industrybase.capability;

import org.jspecify.annotations.Nullable;

import net.industrybase.capability.energy.IElectricPower;
import net.industrybase.capability.energy.IMechanicalTransmit;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.capabilities.BlockCapability;

public class CapabilityList {
	public static final BlockCapability<IMechanicalTransmit, @Nullable Direction> MECHANICAL_TRANSMIT = BlockCapability.createSided(create("mechanical_transmit"), IMechanicalTransmit.class);
	public static final BlockCapability<IElectricPower, @Nullable Direction> ELECTRIC_POWER = BlockCapability.createSided(create("electric_power"), IElectricPower.class);

	private static Identifier create(String path) {
		return Identifier.fromNamespaceAndPath(IndustryBaseApi.MODID, path);
	}

	private CapabilityList() {
	}
}
