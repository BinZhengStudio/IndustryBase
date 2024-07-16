package net.industrybase.api.energy;

import com.google.errorprone.annotations.CanIgnoreReturnValue;

/**
 * ME（MechanicalEnergy）能量系统
 * <p><strong>ME 是功率单位，不是能量单位</strong>
 * <p>1 ME = π W ≈ 0.062831853072 FE/tick (1.25663706144 FE/s)
 */
public interface IMechanicalTransmit {
	/**
	 * 获取功率，无输出设置 0 即可
	 * @return 功率
	 */
	int getPower();

	@CanIgnoreReturnValue
	int setPower(int power);

	/**
	 * 获取阻力数值，无单位。
	 * @return 阻力
	 */
	int getResistance();

	@CanIgnoreReturnValue
	int setResistance(int resistance);

	double getSpeed();
}
