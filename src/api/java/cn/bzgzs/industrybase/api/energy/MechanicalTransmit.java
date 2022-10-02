package cn.bzgzs.industrybase.api.energy;

import cn.bzgzs.industrybase.api.util.TransmitNetwork;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.util.INBTSerializable;

import java.util.Optional;

public class MechanicalTransmit implements IMechanicalTransmit, INBTSerializable<CompoundTag> {
	private int tmpPower;
	private int tmpResistance;
	private final int initPower;
	private final int initResistance;
	private final BlockEntity blockEntity;
	private final BlockPos pos;
	private TransmitNetwork network;

	public MechanicalTransmit(BlockEntity blockEntity, int initPower, int initResistance) {
		this.blockEntity = blockEntity;
		this.pos = blockEntity.getBlockPos();
		this.initPower = initPower;
		this.initResistance = initResistance;
	}

	public void registerToNetwork() {
		Optional.ofNullable(this.blockEntity.getLevel()).ifPresent(level -> {
			if (!level.isClientSide) {
				this.network = TransmitNetwork.Manager.get(level);
				this.setPower(this.tmpPower);
				this.setResistance(this.tmpResistance);
				network.addOrChangeBlock(this.pos, this.blockEntity::setChanged); // TODO ?
			}
		});
	}

	public void removeFromNetwork() {
		Optional.ofNullable(this.blockEntity.getLevel()).ifPresent(level -> {
			if (!level.isClientSide) {
				this.setPower(0);
				this.setResistance(0);
				network.removeBlock(this.pos, this.blockEntity::setChanged);
			}
		});
	}

	@Override
	public int getPower() {
		return this.network.getMachinePower(this.pos);
	}

	public int setPower(int power) {
		int diff = this.network.setMachinePower(this.pos, power);
		if (diff != 0) {
			this.blockEntity.setChanged();
		}
		return diff;
	}

	@Override
	public int getResistance() {
		return this.network.getMachineResistance(this.pos);
	}

	public int setResistance(int resistance) {
		int diff = this.network.setMachineResistance(this.pos, resistance);
		if (diff != 0) {
			this.blockEntity.setChanged();
		}
		return diff;
	}

	@Override
	public double getSpeed() {
		return this.network.speed(this.pos);
	}

	@Override
	public CompoundTag serializeNBT() {
		CompoundTag tag = new CompoundTag();
		tag.putInt("Power", this.getPower());
		tag.putInt("Resistance", this.getResistance());
		return tag;
	}

	@Override
	public void deserializeNBT(CompoundTag nbt) {
		if (nbt.contains("Power")) {
			this.tmpPower = nbt.getInt("Power");
		} else {
			this.tmpPower = initPower;
		}
		if (nbt.contains("Resistance")) {
			this.tmpResistance = nbt.getInt("Resistance");
		} else {
			this.tmpResistance = initResistance;
		}
	}
}
