package net.industrybase.capability.util;

public class ElectricHelper {
	public static double fromTransmit(double speed, int resistance) {
		return speed * resistance * Math.PI / 50.0D;
	}
}
