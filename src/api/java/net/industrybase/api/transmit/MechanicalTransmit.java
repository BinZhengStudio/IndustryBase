package net.industrybase.api.transmit;

import net.industrybase.api.energy.IMechanicalTransmit;
import net.industrybase.api.network.ApiNetworkManager;
import net.industrybase.api.network.client.UnsubscribeSpeedPacket;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.util.LazyOptional;

import javax.annotation.Nullable;
import java.util.Optional;

public class MechanicalTransmit implements IMechanicalTransmit {
	private int tmpPower;
	private int tmpResistance;
	private final BlockEntity blockEntity;
	private final BlockPos pos;
	@Nullable
	private TransmitNetwork network;
	private final LazyOptional<IMechanicalTransmit> lazyOptional;

	public MechanicalTransmit(BlockEntity blockEntity) {
		this.blockEntity = blockEntity;
		this.pos = blockEntity.getBlockPos();
		this.lazyOptional = LazyOptional.of(() -> this);
	}

	public <X> LazyOptional<X> cast() {
		return this.lazyOptional.cast();
	}

	/**
	 * 向传动网络注册该方块。
	 * 需要在 {@link BlockEntity#onLoad()} 中执行一次。
	 */
	public void register() {
		Optional.ofNullable(this.blockEntity.getLevel()).ifPresent(level -> {
			this.network = TransmitNetwork.Manager.get(level);
			if (!level.isClientSide) {
				this.setPower(this.tmpPower);
				this.setResistance(this.tmpResistance);
				this.network.addOrChangeBlock(this.pos, this.blockEntity::setChanged);
			}
		});
	}

	/**
	 * 将此方块从传动网络中移除。
	 * 在 {@link BlockEntity#setRemoved()} 中执行。
	 */
	public void remove() {
		Optional.ofNullable(this.blockEntity.getLevel()).ifPresent(level -> {
			if (this.network != null) {
				if (level.isClientSide) {
					this.network.removeClientSubscribe(this.pos);
					if (this.network.shouldSendUnsubscribePacket(this.pos)) {
						ApiNetworkManager.INSTANCE.sendToServer(new UnsubscribeSpeedPacket(this.pos));
					}
				} else {
					this.network.removeBlock(this.pos, this.blockEntity::setChanged);
				}
			}
		});
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
	 * @param power 要设置的输出功率
	 * @return 原功率与新设功率的差值
	 */
	@Override
	@CanIgnoreReturnValue
	public int setPower(int power) {
		if (!this.blockEntity.getLevel().isClientSide) {
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
		if (!this.blockEntity.getLevel().isClientSide) {
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

	@CanIgnoreReturnValue
	public MechanicalTransmit readFromNBT(CompoundTag tag) {
		CompoundTag nbt = tag.getCompound("MechanicalTransmit");
		this.tmpPower = nbt.getInt("Power");
		this.tmpResistance = nbt.getInt("Resistance");
		return this;
	}

	@CanIgnoreReturnValue
	public CompoundTag writeToNBT(CompoundTag tag) {
		CompoundTag nbt = new CompoundTag();
		if (this.network != null) { // TODO 1.19.3 可能不用
			nbt.putInt("Power", this.getPower());
			nbt.putInt("Resistance", this.getResistance());
		} else {
			nbt.putInt("Power", this.tmpPower);
			nbt.putInt("Resistance", this.tmpResistance);
		}
		tag.put("MechanicalTransmit", nbt);
		return tag;
	}
}