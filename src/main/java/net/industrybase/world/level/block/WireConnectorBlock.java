package net.industrybase.world.level.block;

import com.mojang.serialization.MapCodec;
import net.industrybase.api.electric.ElectricNetwork;
import net.industrybase.api.electric.IWireConnectable;
import net.industrybase.api.util.ElectricHelper;
import net.industrybase.world.item.ItemList;
import net.industrybase.world.level.block.entity.WireConnectorBlockEntity;
import com.google.common.collect.ImmutableMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Containers;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

public class WireConnectorBlock extends BaseEntityBlock {
	public static final MapCodec<WireConnectorBlock> CODEC = simpleCodec((properties) -> new WireConnectorBlock());
	public static final DirectionProperty FACING = BlockStateProperties.FACING;
	private static final VoxelShape CORE = Block.box(4.0D, 4.0D, 4.0D, 12.0D, 12.0D, 12.0D);
	private static final Map<Direction, VoxelShape> SHAPES_DIRECTION = new EnumMap<>(ImmutableMap.of(
			Direction.NORTH, Block.box(4.0D, 4.0D, 0.0D, 12.0D, 12.0D, 4.0D),
			Direction.EAST, Block.box(12.0D, 4.0D, 4.0D, 16.0D, 12.0D, 12.0D),
			Direction.SOUTH, Block.box(4.0D, 4.0D, 12.0D, 12.0D, 12.0D, 16.0D),
			Direction.WEST, Block.box(0.0D, 4.0D, 4.0D, 4.0D, 12.0D, 12.0D),
			Direction.UP, Block.box(4.0D, 12.0D, 4.0D, 12.0D, 16.0D, 12.0D),
			Direction.DOWN, Block.box(4.0D, 0.0D, 4.0D, 12.0D, 4.0D, 12.0D)));
	private static final Map<BlockState, VoxelShape> SHAPES = new HashMap<>();

	protected WireConnectorBlock() {
		super(Properties.ofFullCopy(BlockList.WIRE.get()));
		this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.DOWN));

		for (BlockState state : this.getStateDefinition().getPossibleStates()) {
			SHAPES.put(state, Shapes.or(CORE, SHAPES_DIRECTION.get(state.getValue(FACING))));
		}
	}

	@Override
	@SuppressWarnings("deprecation")
	public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
		if (!state.is(newState.getBlock())) {
			BlockEntity blockEntity = level.getBlockEntity(pos);
			if (blockEntity instanceof IWireConnectable) {
				if (level instanceof ServerLevel) {
					ElectricNetwork network = ElectricNetwork.Manager.get(level);
					network.getWireConn(pos).forEach(blockPos -> {
						ItemStack coil = new ItemStack(ItemList.WIRE_COIL.get());
						coil.setDamageValue(coil.getMaxDamage() - (int) Math.sqrt(pos.distSqr(blockPos))); // 设置耐久
						Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), coil);
					});
				}
			}
			super.onRemove(state, level, pos, newState, isMoving);
		}
		ElectricHelper.updateOnRemove(level, state, newState, pos);
	}

	@SuppressWarnings("deprecation")
	@Override
	public void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos fromPos, boolean isMoving) {
		super.neighborChanged(state, level, pos, block, fromPos, isMoving);
		if (!level.isClientSide()) {
			BlockEntity blockEntity = level.getBlockEntity(pos);
			if (blockEntity != null) {
				ElectricNetwork.Manager.get(level).addOrChangeBlock(pos, blockEntity::setChanged);
			}
		}
	}

	@Nullable
	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		return this.defaultBlockState().setValue(FACING, context.getClickedFace().getOpposite());
	}

	@Override
	@SuppressWarnings("deprecation")
	public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
		return SHAPES.get(state);
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(FACING);
	}

	@Override
	protected MapCodec<? extends BaseEntityBlock> codec() {
		return CODEC;
	}

	@Override
	public RenderShape getRenderShape(BlockState state) {
		return RenderShape.MODEL;
	}

	@Nullable
	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new WireConnectorBlockEntity(pos, state);
	}
}
