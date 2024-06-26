package net.industrybase.api;

import net.industrybase.api.energy.IElectricPower;
import net.industrybase.api.energy.IMechanicalTransmit;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;

public class CapabilityList {
	public static final Capability<IMechanicalTransmit> MECHANICAL_TRANSMIT = CapabilityManager.get(new CapabilityToken<>(){});
	public static final Capability<IElectricPower> ELECTRIC_POWER = CapabilityManager.get(new CapabilityToken<>(){});
}
