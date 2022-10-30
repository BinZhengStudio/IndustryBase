package cn.bzgzs.industrybase.api.electric;

import cn.bzgzs.industrybase.api.energy.IElectricPower;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class ElectricPower implements IElectricPower {
	private double tmpOutputPower;
	private double tmpInputPower;
	private final BlockEntity blockEntity;
	private final BlockPos pos;
	@Nullable
	private ElectricNetwork network;
	private final LazyOptional<IElectricPower> lazyOptional;

	public ElectricPower(BlockEntity blockEntity) {
		this.blockEntity = blockEntity;
		this.pos = blockEntity.getBlockPos();
		this.lazyOptional = LazyOptional.of(() -> this);
	}

	public <X> LazyOptional<X> cast() {
		return this.lazyOptional.cast();
	}

	public void registerToNetwork() {
		Optional.ofNullable(this.blockEntity.getLevel()).ifPresent(level -> {
			this.network = ElectricNetwork.Manager.get(level);
			if (!level.isClientSide) {
				this.setOutputPower(this.tmpOutputPower);
				this.setInputPower(this.tmpInputPower);
				network.addOrChangeBlock(this.pos, this.blockEntity::setChanged);
			}
		});
	}

	public void removeFromNetwork() {
		Optional.ofNullable(this.blockEntity.getLevel()).ifPresent(level -> {
			if (this.network != null && !level.isClientSide) {
				this.network.removeBlock(this.pos, this.blockEntity::setChanged);
			}
		});
	}

	@Override
	public double getOutputPower() {
		return this.network.getMachineOutput(this.pos);
	}

	@Override
	public double setOutputPower(double power) {
		double diff = this.network.setMachineOutput(this.pos, power);
		if (diff != 0) this.blockEntity.setChanged();
		return diff;
	}

	@Override
	public double getInputPower() {
		return this.network.getMachineInput(this.pos);
	}

	@Override
	public double setInputPower(double power) {
		double diff = this.network.setMachineInput(this.pos, power);
		if (diff != 0) this.blockEntity.setChanged();
		return diff;
	}

	@Override
	public double getRealInput() {
		return this.network.getRealInput(this.pos);
	}

	@Override
	public boolean canExtract() {
		return this.getOutputPower() > 0.0D;
	}

	@Override
	public boolean canReceive() {
		return this.getInputPower() > 0.0D;
	}

	@CanIgnoreReturnValue
	public ElectricPower readFromNBT(CompoundTag tag) {
		CompoundTag nbt = tag.getCompound("ElectricPower");
		this.tmpOutputPower = nbt.getDouble("Output");
		this.tmpInputPower = nbt.getDouble("Input");
		return this;
	}

	@CanIgnoreReturnValue
	public CompoundTag writeToNBT(CompoundTag tag) {
		CompoundTag nbt = new CompoundTag();
		if (this.network != null) {
			nbt.putDouble("Output", this.getOutputPower());
			nbt.putDouble("Input", this.getInputPower());
		} else {
			nbt.putDouble("Output", this.tmpOutputPower);
			nbt.putDouble("Input", this.tmpInputPower);
		}
		tag.put("ElectricPower", nbt);
		return tag;
	}
}
