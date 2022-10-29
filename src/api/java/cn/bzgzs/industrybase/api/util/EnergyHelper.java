package cn.bzgzs.industrybase.api.util;

public class EnergyHelper {
	public static double transmitToElectric(double speed, int resistance) {
		return speed * resistance * Math.PI / 50.0D;
	}

	public static int electricToTransmit(double electricPower) {
		return (int) (electricPower * 50 / Math.PI);
	}
}
