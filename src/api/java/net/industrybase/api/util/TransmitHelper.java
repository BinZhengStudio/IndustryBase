package net.industrybase.api.util;

public class TransmitHelper {
	public static int fromElectric(double electricPower) {
		return (int) (electricPower * 50 / Math.PI);
	}
}
