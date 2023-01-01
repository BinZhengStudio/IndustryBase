package cn.bzgzs.industrybase.world.level.block;

import cn.bzgzs.industrybase.network.NetworkManager;
import cn.bzgzs.industrybase.network.server.WaterAmountPacket;
import cn.bzgzs.industrybase.world.level.block.entity.BlockEntityTypeList;
import cn.bzgzs.industrybase.world.level.block.entity.SteamEngineBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import net.minecraftforge.network.PacketDistributor;
import org.jetbrains.annotations.Nullable;

public class SteamEngineBlock extends BaseEntityBlock {
	public static final EnumProperty<Direction.Axis> AXIS = BlockStateProperties.AXIS;
	public static final BooleanProperty LIT = BlockStateProperties.LIT;

	protected SteamEngineBlock() {
		super(Properties.copy(Blocks.IRON_BLOCK).noOcclusion().lightLevel(state -> state.getValue(LIT) ? 13 : 0));
		this.registerDefaultState(this.stateDefinition.any().setValue(AXIS, Direction.Axis.X).setValue(LIT, false));
	}

	@SuppressWarnings("deprecation")
	public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
		if (level.isClientSide) {
			return InteractionResult.SUCCESS;
		} else {
			if (level.getBlockEntity(pos) instanceof SteamEngineBlockEntity blockEntity) {
				ItemStack stack = player.getItemInHand(hand);
				LazyOptional<IFluidHandlerItem> bucket = stack.getCapability(ForgeCapabilities.FLUID_HANDLER_ITEM);
				LazyOptional<IFluidHandler> tank = blockEntity.getCapability(ForgeCapabilities.FLUID_HANDLER, hit.getDirection());
				if (bucket.isPresent() && tank.isPresent()) {
					bucket.ifPresent(itemCapability -> tank.ifPresent(engineCapability -> {
						FluidStack drained = itemCapability.drain(Math.max(0, engineCapability.getTankCapacity(0) - engineCapability.getFluidInTank(0).getAmount()), player.isCreative() ? IFluidHandler.FluidAction.SIMULATE : IFluidHandler.FluidAction.EXECUTE);
						engineCapability.fill(drained, IFluidHandler.FluidAction.EXECUTE);
						// 发包同步
						((ServerLevel) level).getPlayers(playerIn -> true).forEach(playerIn -> NetworkManager.INSTANCE.send(PacketDistributor.PLAYER.with(() -> playerIn), new WaterAmountPacket(pos, engineCapability.getFluidInTank(0).getAmount())));
						if (stack.is(Items.WATER_BUCKET)) { // TODO 兼容性待解决
							if (!player.isCreative()) player.setItemInHand(hand, new ItemStack(Items.BUCKET));
						}
					}));
				} else {
					player.openMenu(blockEntity);
				}
			}
			return InteractionResult.CONSUME;
		}
	}

	@Override
	@SuppressWarnings("deprecation")
	public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
		if (!state.is(newState.getBlock())) {
			BlockEntity blockEntity = level.getBlockEntity(pos);
			if (blockEntity instanceof SteamEngineBlockEntity steamEngine) {
				if (level instanceof ServerLevel) {
					Containers.dropContents(level, pos, steamEngine);
				}
				level.updateNeighbourForOutputSignal(pos, this); // TODO
			}
			super.onRemove(state, level, pos, newState, isMoving);
		}
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(AXIS, LIT);
	}

	@Nullable
	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		return this.defaultBlockState().setValue(AXIS, context.getClickedFace().getAxis());
	}

	@Override
	@SuppressWarnings("deprecation")
	public RenderShape getRenderShape(BlockState state) {
		return RenderShape.MODEL;
	}

	@Nullable
	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new SteamEngineBlockEntity(pos, state);
	}

	@Nullable
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> serverType) {
		return level.isClientSide ? null : createTickerHelper(serverType, BlockEntityTypeList.STEAM_ENGINE.get(), SteamEngineBlockEntity::serverTick);
	}
}
