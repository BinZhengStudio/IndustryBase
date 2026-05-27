package net.industrybase.api.transmit;

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import net.industrybase.api.energy.IMechanicalTransmit;
import net.industrybase.api.network.client.UnsubscribeSpeedPacket;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import net.neoforged.neoforge.common.util.ValueIOSerializable;
import org.jspecify.annotations.Nullable;

public class MechanicalTransmit implements IMechanicalTransmit, ValueIOSerializable {
	private int tmpPower;
	private int tmpResistance;
	@Nullable
	private Level level;
	private final BlockPos pos;
	private final BlockEntity blockEntity;
	@Nullable
	private TransmitNetwork network;

	public MechanicalTransmit(BlockEntity blockEntity) {
		this.blockEntity = blockEntity;
		this.pos = blockEntity.getBlockPos();
	}

	/**
	 * 向传动网络注册该方块。
	 * 需要在 {@link BlockEntity#onLoad()} 中执行一次。
	 */
	public void register() {
		this.level = this.blockEntity.getLevel();
		if (this.level != null) {
			this.network = TransmitNetwork.Manager.get(this.level);
			if (!this.level.isClientSide()) {
				this.setPower(this.tmpPower);
				this.setResistance(this.tmpResistance);
				this.network.addOrChangeBlock(this.pos, this.blockEntity::setChanged);
			}
		}
	}

	/**
	 * 将此方块从传动网络中移除。
	 * 在 {@link BlockEntity#setRemoved()} 中执行。
	 */
	public void remove() {
		if (this.level != null) {
			if (this.network != null) {
				if (this.level.isClientSide()) {
					this.network.removeClientSubscribe(this.pos);
					if (this.network.shouldSendUnsubscribePacket(this.pos)) {
						ClientPacketDistributor.sendToServer(new UnsubscribeSpeedPacket(this.pos));
					}
				} else {
					this.network.removeBlock(this.pos, this.blockEntity::setChanged);
				}
			}
		}
	}

	@Nullable
	public TransmitNetwork getNetwork() {
		return this.network;
	}

	@Override
	public int getPower() {
		return this.network.getMachinePower(this.pos);
	}

	/**
	 * 设置方块的输出功率。
	 *
	 * @param power 要设置的输出功率
	 * @return 原功率与新设功率的差值
	 */
	@Override
	@CanIgnoreReturnValue
	public int setPower(int power) {
		if (!this.level.isClientSide()) {
			int diff = this.network.setMachinePower(this.pos, power);
			if (diff != 0) this.blockEntity.setChanged();
			return diff;
		}
		return 0;
	}

	@Override
	public int getResistance() {
		return this.network.getMachineResistance(this.pos);
	}

	@Override
	@CanIgnoreReturnValue
	public int setResistance(int resistance) {
		if (!this.level.isClientSide()) {
			int diff = this.network.setMachineResistance(this.pos, resistance);
			if (diff != 0) {
				this.blockEntity.setChanged();
			}
			return diff;
		}
		return 0;
	}

	@Override
	public double getSpeed() {
		return this.network.speed(this.pos);
	}

    @Override
    public void serialize(ValueOutput output) {
        output.putInt("Power", this.getPower());
        output.putInt("Resistance", this.getResistance());
    }

    @Override
    public void deserialize(ValueInput input) {
        this.tmpPower = input.getIntOr("Power", 0);
        this.tmpResistance = input.getIntOr("Resistance", 0);
    }
}
