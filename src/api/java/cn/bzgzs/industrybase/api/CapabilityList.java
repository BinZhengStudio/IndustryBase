package cn.bzgzs.industrybase.api;

import cn.bzgzs.industrybase.api.energy.IMechanicalTransmit;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;

public class CapabilityList {
	public static final Capability<IMechanicalTransmit> MECHANICAL_TRANSMIT = CapabilityManager.get(new CapabilityToken<>(){});
}
