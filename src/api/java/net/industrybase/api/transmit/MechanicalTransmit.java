package net.industrybase.api.transmit;

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import net.industrybase.api.energy.IMechanicalTransmit;
import net.industrybase.api.network.client.UnsubscribeSpeedPacket;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.common.util.INBTSerializable;
import net.neoforged.neoforge.network.PacketDistributor;

import javax.annotation.Nullable;

public class MechanicalTransmit implements IMechanicalTransmit, INBTSerializable<CompoundTag> {
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
			if (!this.level.isClientSide) {
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
				if (this.level.isClientSide) {
					this.network.removeClientSubscribe(this.pos);
					if (this.network.shouldSendUnsubscribePacket(this.pos)) {
						PacketDistributor.sendToServer(new UnsubscribeSpeedPacket(this.pos));
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
		if (!this.level.isClientSide) {
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
		if (!this.level.isClientSide) {
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
	public CompoundTag serializeNBT(@Nullable HolderLookup.Provider provider) {
		CompoundTag nbt = new CompoundTag();
		nbt.putInt("Power", this.getPower());
		nbt.putInt("Resistance", this.getResistance());
		return nbt;
	}

	@Override
	public void deserializeNBT(@Nullable HolderLookup.Provider provider, CompoundTag nbt) {
		this.tmpPower = nbt.getInt("Power");
		this.tmpResistance = nbt.getInt("Resistance");
	}

	public void readFromNBT(CompoundTag tag) {
		CompoundTag nbt = tag.getCompound("MechanicalTransmit");
		this.deserializeNBT(null, nbt);
	}

	public void writeToNBT(CompoundTag tag) {
		CompoundTag nbt = new CompoundTag();
		if (this.network != null) {
			nbt.putInt("Power", this.getPower());
			nbt.putInt("Resistance", this.getResistance());
		}
		tag.put("MechanicalTransmit", nbt);
	}
}