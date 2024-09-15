package net.industrybase.world.level.block.entity;

import net.industrybase.api.electric.ElectricPower;
import net.industrybase.api.electric.IWireConnectable;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.HashSet;
import java.util.Set;

public class InsulatorBlockEntity extends BlockEntity implements IWireConnectable {
	private final ElectricPower electricPower = new ElectricPower(this);
	private boolean subscribed = false;

	public InsulatorBlockEntity(BlockPos pos, BlockState blockState) {
		super(BlockEntityTypeList.INSULATOR.get(), pos, blockState);
	}

	@Override
	public void onLoad() {
		super.onLoad();
		this.electricPower.register();
	}

	@Override
	public void setRemoved() {
		this.electricPower.remove();
		super.setRemoved();
	}

	@Override
	public boolean isSubscribed() {
		return this.subscribed;
	}

	@Override
	public void setSubscribed() {
		this.subscribed = true;
	}

	@Override
	public Set<BlockPos> getWires() {
		if (this.electricPower.getNetwork() != null) {
			return this.electricPower.getNetwork().getWireConn(this.worldPosition);
		}
		return new HashSet<>();
	}
}
