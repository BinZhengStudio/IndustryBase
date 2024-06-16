package net.industrybase.world.level.block;

import com.mojang.serialization.MapCodec;
import net.industrybase.api.CapabilityList;
import net.industrybase.api.electric.ElectricNetwork;
import net.industrybase.world.level.block.entity.WireBlockEntity;
import com.google.common.collect.ImmutableMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import org.jetbrains.annotations.Nullable;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

public class WireBlock extends BaseEntityBlock {
	public static final MapCodec<WireBlock> CODEC = simpleCodec((properties) -> new WireBlock());

	public static final EnumMap<Direction, BooleanProperty> PROPERTIES = new EnumMap<>(ImmutableMap.of(
			Direction.NORTH, BlockStateProperties.NORTH,
			Direction.EAST, BlockStateProperties.EAST,
			Direction.SOUTH, BlockStateProperties.SOUTH,
			Direction.WEST, BlockStateProperties.WEST,
			Direction.UP, BlockStateProperties.UP,
			Direction.DOWN, BlockStateProperties.DOWN
	));

	private static final VoxelShape CORE = Block.box(6.0D, 6.0D, 6.0D, 10.0D, 10.0D, 10.0D);
	private static final EnumMap<Direction, VoxelShape> SHAPES_DIRECTION = new EnumMap<>(ImmutableMap.of(
			Direction.NORTH, Block.box(4.0D, 4.0D, 0.0D, 12.0D, 12.0D, 10.0D),
			Direction.EAST, Block.box(6.0D, 4.0D, 4.0D, 16.0D, 12.0D, 12.0D),
			Direction.SOUTH, Block.box(4.0D, 4.0D, 6.0D, 12.0D, 12.0D, 16.0D),
			Direction.WEST, Block.box(0.0D, 4.0D, 4.0D, 10.0D, 12.0D, 12.0D),
			Direction.UP, Block.box(4.0D, 6.0D, 4.0D, 12.0D, 16.0D, 12.0D),
			Direction.DOWN, Block.box(4.0D, 0.0D, 4.0D, 12.0D, 10.0D, 12.0D)));
	private static final HashMap<BlockState, VoxelShape> SHAPES = new HashMap<>();

	private static final EnumMap<Direction, VoxelShape> COLLISION_SHAPES_DIRECTION = new EnumMap<>(ImmutableMap.of(
			Direction.NORTH, Block.box(6.0D, 6.0D, 0.0D, 10.0D, 10.0D, 6.0D),
			Direction.EAST, Block.box(10.0D, 6.0D, 6.0D, 16.0D, 10.0D, 10.0D),
			Direction.SOUTH, Block.box(6.0D, 6.0D, 10.0D, 10.0D, 10.0D, 16.0D),
			Direction.WEST, Block.box(0.0D, 6.0D, 6.0D, 6.0D, 10.0D, 10.0D),
			Direction.UP, Block.box(6.0D, 10.0D, 6.0D, 10.0D, 16.0D, 10.0D),
			Direction.DOWN, Block.box(6.0D, 0.0D, 6.0D, 10.0D, 6.0D, 10.0D)));
	private static final HashMap<BlockState, VoxelShape> COLLISION_SHAPES = new HashMap<>();

	protected WireBlock() {
		super(Properties.of().strength(0.5F).sound(SoundType.METAL).noOcclusion());

		BlockState defaultState = this.stateDefinition.any();
		for (Direction direction : Direction.values()) {
			defaultState.setValue(PROPERTIES.get(direction), false);
		}
		this.registerDefaultState(defaultState);

		// 计算好所有 BlockState 的碰撞箱
		for(BlockState state : this.getStateDefinition().getPossibleStates()) {
			SHAPES.put(state, this.calculateShape(state, SHAPES_DIRECTION));
			COLLISION_SHAPES.put(state, this.calculateShape(state, COLLISION_SHAPES_DIRECTION));
		}
	}

	private VoxelShape calculateShape(BlockState state, Map<Direction, VoxelShape> map) {
		VoxelShape shape = CORE;
		for(Direction direction : Direction.values()) {
			if (state.getValue(PROPERTIES.get(direction))) {
				shape = Shapes.or(shape, map.get(direction));
			}
		}
		return shape;
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
		BlockState state = this.defaultBlockState();
		for (Direction direction : Direction.values()) {
			Level level = context.getLevel();
			BlockPos facingPos = context.getClickedPos().relative(direction);
			BlockState facingState = level.getBlockState(facingPos);
			state = state.setValue(PROPERTIES.get(direction), this.canConnect(level, direction.getOpposite(), facingPos, facingState));
		}
		return state;
	}

	@Override
	@SuppressWarnings("deprecation")
	public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
		return SHAPES.get(state);
	}

	@Override
	@SuppressWarnings("deprecation")
	public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
		return COLLISION_SHAPES.get(state);
	}

	@Override
	@SuppressWarnings("deprecation")
	public VoxelShape getOcclusionShape(BlockState state, BlockGetter level, BlockPos pos) {
		return COLLISION_SHAPES.get(state);
	}

	@Override
	@SuppressWarnings("deprecation")
	public BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor level, BlockPos currentPos, BlockPos neighborPos) {
		return state.setValue(PROPERTIES.get(direction), this.canConnect(level, direction.getOpposite(), neighborPos, neighborState));
	}

	private boolean canConnect(LevelAccessor level, Direction facing, BlockPos pos, BlockState state) {
		if (!state.is(this)) {
			BlockEntity blockEntity = level.getBlockEntity(pos);
			return blockEntity != null && (blockEntity.getCapability(ForgeCapabilities.ENERGY, facing).isPresent() ||
					blockEntity.getCapability(CapabilityList.ELECTRIC_POWER, facing).isPresent());
		}
		return true;
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(PROPERTIES.values().toArray(new BooleanProperty[0]));
	}

	@Override
	protected MapCodec<? extends BaseEntityBlock> codec() {
		return CODEC;
	}

	@Override
	@SuppressWarnings("deprecation")
	public RenderShape getRenderShape(BlockState pState) {
		return RenderShape.MODEL;
	}

	@Nullable
	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new WireBlockEntity(pos, state);
	}
}
