package net.industrybase.world.level.block.entity;

import net.industrybase.api.electric.ElectricPower;
import net.industrybase.api.transmit.MechanicalTransmit;
import net.industrybase.world.level.block.DynamoBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;

public class CreativeDynamoBlockEntity extends BlockEntity {
	private static final int RESISTANCE = 2;
	private final MechanicalTransmit transmit = new MechanicalTransmit(this);
	private final ElectricPower electricPower = new ElectricPower(this);

	public CreativeDynamoBlockEntity(BlockPos pos, BlockState state) {
		super(BlockEntityTypeList.CREATIVE_DYNAMO.get(), pos, state);
	}

	@Override
	public void onLoad() {
		super.onLoad();
		this.transmit.register();
		this.transmit.setResistance(RESISTANCE);
		this.electricPower.register();
		this.electricPower.setOutputPower(10.0D);
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
}
