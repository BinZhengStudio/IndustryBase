package net.industrybase.world.level.block.entity;

import net.industrybase.api.electric.ElectricPower;
import net.industrybase.api.transmit.MechanicalTransmit;
import net.industrybase.api.util.TransmitHelper;
import net.industrybase.world.level.block.ElectricMotorBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;

public class ElectricMotorBlockEntity extends BlockEntity {
	private static final int RESISTANCE = 2;
	private final MechanicalTransmit transmit = new MechanicalTransmit(this);
	private final ElectricPower electricPower = new ElectricPower(this);

	public ElectricMotorBlockEntity(BlockPos pos, BlockState state) {
		super(BlockEntityTypeList.ELECTRIC_MOTOR.get(), pos, state);
	}

	@Override
	public void onLoad() {
		super.onLoad();
		this.transmit.register();
		this.transmit.setResistance(RESISTANCE);
		this.electricPower.register();
		this.electricPower.setInputPower(2.0D);
	}

	public static void serverTick(Level level, BlockPos pos, BlockState state, ElectricMotorBlockEntity blockEntity) {
		int maxPower = TransmitHelper.fromElectric(blockEntity.electricPower.getRealInput());
		int power = blockEntity.transmit.getPower();
		if (power < maxPower) {
			blockEntity.transmit.setPower(power + 1);
		} else if (power > 0 && power > maxPower) {
			blockEntity.transmit.setPower(power - 1);
		}
	}

	@Nullable
	public MechanicalTransmit getTransmit(Direction side) {
		if (side == this.getBlockState().getValue(ElectricMotorBlock.FACING)) {
			return this.transmit;
		}
		return null;
	}

	@Nullable
	public ElectricPower getElectricPower(Direction side) {
		if (side == this.getBlockState().getValue(ElectricMotorBlock.FACING).getOpposite()) {
			return this.electricPower;
		}
		return null;
	}

	@Override
	public void setRemoved() {
		this.transmit.remove();
		this.electricPower.remove();
		super.setRemoved();
	}

	@Override
	public void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
		super.loadAdditional(tag, registries);
		this.transmit.readFromNBT(tag);
		this.electricPower.readFromNBT(tag);
	}

	@Override
	protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
		super.saveAdditional(tag, registries);
		this.transmit.writeToNBT(tag);
		this.electricPower.writeToNBT(tag);
	}
}
