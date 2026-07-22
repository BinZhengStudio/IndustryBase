package net.industrybase.world.level.block.entity;

import net.industrybase.capability.electric.ElectricNetwork;
import net.industrybase.capability.electric.ElectricPower;
import net.industrybase.capability.electric.IWireConnectable;
import net.industrybase.network.client.SubscribeWireConnPacket;
import net.industrybase.world.item.ItemList;
import net.industrybase.world.level.block.DynamoBlock;
import net.industrybase.world.level.block.WireConnectorBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.Containers;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;

import org.jspecify.annotations.Nullable;
import java.util.HashSet;
import java.util.Set;

public class WireConnectorBlockEntity extends BlockEntity implements IWireConnectable {
	private final ElectricPower electricPower = new ElectricPower(this);
	private boolean subscribed = false;

	public WireConnectorBlockEntity(BlockPos pos, BlockState state) {
		super(BlockEntityTypeList.WIRE_CONNECTOR.get(), pos, state);
	}

	@Override
	public void onLoad() {
		super.onLoad();
		this.electricPower.register();
        if (this.level.isClientSide()) {
            ClientPacketDistributor.sendToServer(new SubscribeWireConnPacket(this.worldPosition));
        }
	}

	@Nullable
	public ElectricPower getElectricPower(Direction side) {
		if (side == this.getBlockState().getValue(DynamoBlock.FACING)) {
			return this.electricPower;
		}
		return null;
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

    @Override
    public void preRemoveSideEffects(BlockPos pos, BlockState state) {
        super.preRemoveSideEffects(pos, state);
        if (!this.level.isClientSide()) {
            ElectricNetwork network = ElectricNetwork.Manager.get(level);
            network.getWireConn(pos).forEach(blockPos -> {
                ItemStack coil = new ItemStack(ItemList.WIRE_COIL.get());
                coil.setDamageValue(coil.getMaxDamage() - (int) Math.sqrt(pos.distSqr(blockPos))); // 设置耐久
                Containers.dropItemStack(this.level, pos.getX(), pos.getY(), pos.getZ(), coil);
            });
        }
    }

    @Override
    @SuppressWarnings("deprecation")
    public void setBlockState(BlockState blockState) {
        var oldState = this.getBlockState();
        super.setBlockState(blockState);
        if (oldState.getValue(WireConnectorBlock.FACING) != blockState.getValue(WireConnectorBlock.FACING)) {
            ElectricNetwork.Manager.get(level).addOrChangeBlock(this.worldPosition, this::invalidateCapabilities);
        }
    }
}
