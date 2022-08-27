package cn.bzgzs.largeprojects.api.energy;

import net.minecraftforge.common.capabilities.AutoRegisterCapability;

@AutoRegisterCapability
public interface IMechanicalTransmit {
	/**
	 * 获取转速，单位为rps（1s=20tick）
	 * @return 每秒转动圈数
	 */
	default double getSpeed() {
		return (double) getPower() / getTorque();
	}

	/**
	 * 获取功率，单位ME(MechanicalEnergy)/s，仅限输出源需要设置
	 * @return 功率
	 */
	int getPower();

	/**
	 * 获取扭力，无单位
	 * @return 扭力
	 */
	float getTorque();

	/**
	 * 获取阻力数值，无单位。
	 * @return 阻力
	 */
	float getResistance();

	/**
	 * 能否输出
	 */
	boolean canExtract();

	/**
	 * 能否输入
	 */
	boolean canReceive();
}
