package cn.bzgzs.industrybase.world.level.block.entity;

import cn.bzgzs.industrybase.api.CapabilityList;
import cn.bzgzs.industrybase.api.electric.ElectricPower;
import cn.bzgzs.industrybase.api.transmit.MechanicalTransmit;
import cn.bzgzs.industrybase.api.util.TransmitHelper;
import cn.bzgzs.industrybase.world.level.block.ElectricMotorBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
		this.electricPower.setInputPower(5.0D);
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

	@Override
	public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
		if (side == this.getBlockState().getValue(ElectricMotorBlock.FACING)) {
			return cap == CapabilityList.MECHANICAL_TRANSMIT ? this.transmit.cast() : super.getCapability(cap, side);
		} else if (side == this.getBlockState().getValue(ElectricMotorBlock.FACING).getOpposite()) {
			return this.electricPower.cast(cap, super.getCapability(cap, side));
		}
		return super.getCapability(cap, side);
	}

	@Override
	public void setRemoved() {
		this.transmit.remove();
		this.electricPower.remove();
		super.setRemoved();
	}

	@Override
	public void load(CompoundTag tag) {
		super.load(tag);
		this.transmit.readFromNBT(tag);
		this.electricPower.readFromNBT(tag);
	}

	@Override
	protected void saveAdditional(CompoundTag tag) {
		super.saveAdditional(tag);
		this.transmit.writeToNBT(tag);
		this.electricPower.writeToNBT(tag);
	}
}
