package cn.bzgzs.industrybase.api.energy;

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import net.minecraftforge.common.capabilities.AutoRegisterCapability;

/**
 * EP（ElectricPower）能量系统
 * <p><strong>EP 是功率单位，不是能量单位</strong>
 * <p>1 EP = 1 FE/tick (20 FE/s) = 50 W
 */
@AutoRegisterCapability
public interface IElectricPower {
	/**
	 * 获取输出功率，无输出设置 0 即可
	 * @return 功率
	 */
	double getOutputPower();

	@CanIgnoreReturnValue
	double setOutputPower(double power);

	/**
	 * 获取额定输入功率，无输入设置 0 即可
	 * @return 功率
	 */
	double getInputPower();

	@CanIgnoreReturnValue
	double setInputPower(double power);

	double getRealInput();
}
