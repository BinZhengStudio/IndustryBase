package net.industrybase.api;

import net.industrybase.api.energy.IElectricPower;
import net.industrybase.api.energy.IMechanicalTransmit;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.capabilities.BlockCapability;
import org.jetbrains.annotations.Nullable;

public class CapabilityList {
	public static final BlockCapability<IMechanicalTransmit, @Nullable Direction> MECHANICAL_TRANSMIT = BlockCapability.createSided(create("mechanical_transmit"), IMechanicalTransmit.class);
	public static final BlockCapability<IElectricPower, @Nullable Direction> ELECTRIC_POWER = BlockCapability.createSided(create("electric_power"), IElectricPower.class);

	private static ResourceLocation create(String path) {
		return ResourceLocation.fromNamespaceAndPath(IndustryBaseApi.MODID, path);
	}

	private CapabilityList() {
	}
}
