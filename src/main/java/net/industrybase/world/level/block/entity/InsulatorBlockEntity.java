package net.industrybase.world.level.block.entity;

import net.industrybase.capability.electric.ElectricPower;
import net.industrybase.capability.electric.IWireConnectable;
import net.industrybase.network.client.SubscribeWireConnPacket;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;

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
        if (this.level.isClientSide()) {
            ClientPacketDistributor.sendToServer(new SubscribeWireConnPacket(this.worldPosition));
        }
	}

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        input.readChild("Electric", electricPower);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putChild("Electric", electricPower);
    }

	@Override
	public void setRemoved() {
		this.electricPower.remove();
		super.setRemoved();
	}
}
