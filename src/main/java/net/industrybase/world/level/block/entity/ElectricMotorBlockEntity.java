package net.industrybase.world.level.block.entity;

import org.jspecify.annotations.Nullable;

import net.industrybase.api.electric.ElectricNetwork;
import net.industrybase.api.electric.ElectricPower;
import net.industrybase.api.transmit.MechanicalTransmit;
import net.industrybase.api.transmit.TransmitNetwork;
import net.industrybase.api.util.TransmitHelper;
import net.industrybase.world.level.block.CreativeDynamoBlock;
import net.industrybase.world.level.block.ElectricMotorBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public class ElectricMotorBlockEntity extends BlockEntity {
    private static final int RESISTANCE = 2;
	protected final MechanicalTransmit transmit = new MechanicalTransmit(this);
	protected final ElectricPower electricPower = new ElectricPower(this);

	public ElectricMotorBlockEntity(BlockEntityType<?> type, BlockPos worldPosition, BlockState blockState) {
        super(type, worldPosition, blockState);
    }

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
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        input.readChild("Transmit", transmit);
        input.readChild("Electric", electricPower);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putChild("Transmit", transmit);
        output.putChild("Electric", electricPower);
    }

        @Override
    @SuppressWarnings("deprecation")
    public void setBlockState(BlockState blockState) {
        var oldState = this.getBlockState();
        super.setBlockState(blockState);
        if (oldState.getValue(CreativeDynamoBlock.FACING) != blockState.getValue(CreativeDynamoBlock.FACING)) {
            TransmitNetwork.Manager.get(level).addOrChangeBlock(this.worldPosition, () -> {});
            ElectricNetwork.Manager.get(level).addOrChangeBlock(this.worldPosition, this::invalidateCapabilities);
        }
    }
}
