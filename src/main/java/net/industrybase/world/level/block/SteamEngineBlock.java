package net.industrybase.world.level.block;

import com.mojang.serialization.MapCodec;
import net.industrybase.world.level.block.entity.BlockEntityTypeList;
import net.industrybase.world.level.block.entity.SteamEngineBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;

public class SteamEngineBlock extends BaseEntityBlock {
	public static final MapCodec<SteamEngineBlock> CODEC = simpleCodec((properties) -> new SteamEngineBlock());
    public static final EnumProperty<Direction.Axis> AXIS = BlockStateProperties.AXIS;
	public static final BooleanProperty LIT = BlockStateProperties.LIT;

	protected SteamEngineBlock() {
		super(Properties.ofFullCopy(Blocks.IRON_BLOCK).noOcclusion().lightLevel(state -> state.getValue(LIT) ? 13 : 0));
		this.registerDefaultState(this.stateDefinition.any().setValue(AXIS, Direction.Axis.X).setValue(LIT, false));
	}

	@Override
	public InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
		if (level.isClientSide()) {
			return InteractionResult.SUCCESS;
		} else {
			if (level.getBlockEntity(pos) instanceof SteamEngineBlockEntity blockEntity) {
				if (stack.is(Items.WATER_BUCKET)) {
					ResourceHandler<FluidResource> engineTank = level.getCapability(Capabilities.Fluid.BLOCK, pos, state, blockEntity, hitResult.getDirection());
					if (engineTank != null) {
                        try (var tx = Transaction.openRoot()) {
                            engineTank.insert(FluidResource.of(Fluids.WATER), FluidType.BUCKET_VOLUME, tx);
                            tx.commit();
                        }
						if (!player.getAbilities().instabuild) player.setItemInHand(hand, new ItemStack(Items.BUCKET));
					}
					return InteractionResult.CONSUME;
				} else {
					player.openMenu(blockEntity);
				}
			}
			return super.useItemOn(stack, state, level, pos, player, hand, hitResult);
		}
	}

	@Override
	protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
		if (level.isClientSide()) {
			return InteractionResult.SUCCESS;
		} else {
			if (level.getBlockEntity(pos) instanceof SteamEngineBlockEntity blockEntity) {
				player.openMenu(blockEntity);
			}
			return InteractionResult.CONSUME;
		}
	}

    @Override
    protected boolean hasAnalogOutputSignal(BlockState state) {
        return true;
    }

    @Override
    protected int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos, Direction direction) {
        return AbstractContainerMenu.getRedstoneSignalFromBlockEntity(level.getBlockEntity(pos));
    }

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(AXIS, LIT);
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		return this.defaultBlockState().setValue(AXIS, context.getClickedFace().getAxis());
	}

	@Override
	protected MapCodec<? extends BaseEntityBlock> codec() {
		return CODEC;
	}

	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new SteamEngineBlockEntity(pos, state);
	}

	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> serverType) {
		return createTickerHelper(serverType, BlockEntityTypeList.STEAM_ENGINE.get(), level.isClientSide() ? SteamEngineBlockEntity::clientTick : SteamEngineBlockEntity::serverTick);
	}
}
