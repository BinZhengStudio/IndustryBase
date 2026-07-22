package net.industrybase.world.level.block.entity;

import net.industrybase.capability.electric.ElectricNetwork;
import net.industrybase.capability.electric.ElectricPower;
import net.industrybase.capability.transmit.MechanicalTransmit;
import net.industrybase.capability.transmit.TransmitNetwork;
import net.industrybase.capability.util.ElectricHelper;
import net.industrybase.world.level.block.CreativeDynamoBlock;
import net.industrybase.world.level.block.DynamoBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import org.jspecify.annotations.Nullable;

public class DynamoBlockEntity extends BlockEntity {

    private double oldPower;
	private static final int RESISTANCE = 2;
	private final MechanicalTransmit transmit = new MechanicalTransmit(this);
	protected final ElectricPower electricPower = new ElectricPower(this);

	public DynamoBlockEntity(BlockEntityType<?> type, BlockPos worldPosition, BlockState blockState) {
        super(type, worldPosition, blockState);
    }

	public DynamoBlockEntity(BlockPos pos, BlockState state) {
		super(BlockEntityTypeList.DYNAMO.get(), pos, state);
	}

	@Override
	public void onLoad() {
		super.onLoad();
		this.transmit.register();
		this.transmit.setResistance(RESISTANCE);
		this.electricPower.register();
	}

	public static void serverTick(Level level, BlockPos pos, BlockState state, DynamoBlockEntity blockEntity) {
		double power = ElectricHelper.fromTransmit(blockEntity.transmit.getSpeed(), RESISTANCE);
		if (power != blockEntity.oldPower) {
			blockEntity.electricPower.setOutputPower(power);
			blockEntity.oldPower = power;
		}
	}

	@Nullable
	public MechanicalTransmit getTransmit(Direction side) {
		if (side == this.getBlockState().getValue(DynamoBlock.FACING)) {
			return this.transmit;
		}
		return null;
	}

	@Nullable
	public ElectricPower getElectricPower(Direction side) {
		if (side == this.getBlockState().getValue(DynamoBlock.FACING).getOpposite()) {
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
        this.oldPower = input.getDoubleOr("OldPower", 0.0D);
        input.readChild("Transmit", transmit);
        input.readChild("Electric", electricPower);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putDouble("OldPower", this.oldPower);
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
