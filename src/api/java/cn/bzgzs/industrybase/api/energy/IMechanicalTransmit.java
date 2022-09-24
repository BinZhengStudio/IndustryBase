package cn.bzgzs.industrybase.api.energy;

import net.minecraftforge.common.capabilities.AutoRegisterCapability;

@AutoRegisterCapability
public interface IMechanicalTransmit {
	/**
	 * 获取功率，单位ME(MechanicalEnergy)/s，仅限输出源需要设置
	 * @return 功率
	 */
	int getPower();

	/**
	 * 获取阻力数值，无单位。
	 * @return 阻力
	 */
	int getResistance();

	double getSpeed();

	/**
	 * 能否输出
	 */
	boolean canExtract();

	/**
	 * 能否输入
	 */
	boolean canReceive();
}
