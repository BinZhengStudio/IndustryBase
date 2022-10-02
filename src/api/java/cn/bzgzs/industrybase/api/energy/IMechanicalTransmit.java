package cn.bzgzs.industrybase.api.energy;

import net.minecraftforge.common.capabilities.AutoRegisterCapability;

/**
 * ME（MechanicalEnergy）能量系统
 * <p><strong>ME 是功率单位，不是能量单位</strong>
 * <p>1 ME = π W ≈ 0.062831853072 FE/tick (1.25663706144 FE/s)
 */
@AutoRegisterCapability
public interface IMechanicalTransmit {
	/**
	 * 获取功率，无输出设置 0 即可
	 * @return 功率
	 */
	int getPower();

	/**
	 * 获取阻力数值，无单位。
	 * @return 阻力
	 */
	int getResistance();

	double getSpeed();
}
