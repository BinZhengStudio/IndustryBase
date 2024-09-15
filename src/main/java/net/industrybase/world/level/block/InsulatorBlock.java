package net.industrybase.world.level.block;

import com.google.common.collect.ImmutableMap;
import com.mojang.serialization.MapCodec;
import net.industrybase.world.level.block.entity.InsulatorBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

public class InsulatorBlock extends BaseEntityBlock {
	public static final MapCodec<InsulatorBlock> CODEC = simpleCodec((properties) -> new InsulatorBlock());
	public static final BooleanProperty POSITIVE_CONNECTED = BooleanProperty.create("positive_connected");
	public static final BooleanProperty NEGATIVE_CONNECTED = BooleanProperty.create("negative_connected");

	private static final VoxelShape CORE = Block.box(5.5D, 5.5D, 5.5D, 10.5D, 10.5D, 10.5D);
	private static final EnumMap<Direction, VoxelShape> SHAPES_DIRECTION = new EnumMap<>(ImmutableMap.of(
			Direction.NORTH, Block.box(5.5D, 5.5D, 0.0D, 10.5D, 10.5D, 5.5D),
			Direction.EAST, Block.box(10.5D, 5.5D, 5.5D, 16.0D, 10.5D, 10.5D),
			Direction.SOUTH, Block.box(5.5D, 5.5D, 10.5D, 10.5D, 10.5D, 16.0D),
			Direction.WEST, Block.box(0.0D, 5.5D, 5.5D, 5.5D, 10.5D, 10.5D),
			Direction.UP, Block.box(5.5D, 10.5D, 5.5D, 10.5D, 16.0D, 10.5D),
			Direction.DOWN, Block.box(5.5D, 0.0D, 5.5D, 10.5D, 5.5D, 10.5D)));
	private static final HashMap<BlockState, VoxelShape> SHAPES = new HashMap<>();

	protected InsulatorBlock() {
		super(Properties.ofFullCopy(BlockList.WIRE_CONNECTOR.get()));
		this.registerDefaultState(this.stateDefinition.any()
				.setValue(BlockStateProperties.AXIS, Direction.Axis.X)
				.setValue(POSITIVE_CONNECTED, false)
				.setValue(NEGATIVE_CONNECTED, false)
		);

		for (BlockState state : this.getStateDefinition().getPossibleStates()) {
			SHAPES.put(state, this.calculateShape(state, SHAPES_DIRECTION));
		}
	}

	private VoxelShape calculateShape(BlockState state, Map<Direction, VoxelShape> map) {
		VoxelShape shape = CORE;
		Direction.Axis axis = state.getValue(BlockStateProperties.AXIS);
		Direction positive = Direction.fromAxisAndDirection(axis, Direction.AxisDirection.POSITIVE);
		Direction negative = Direction.fromAxisAndDirection(axis, Direction.AxisDirection.NEGATIVE);
		if (state.getValue(POSITIVE_CONNECTED)) shape = Shapes.or(shape, map.get(positive));
		if (state.getValue(NEGATIVE_CONNECTED)) shape = Shapes.or(shape, map.get(negative));
		return shape;
	}

	@Override
	public BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor level, BlockPos currentPos, BlockPos neighborPos) {
		if (!this.canSurvive(state, level, currentPos)) {
			return Blocks.AIR.defaultBlockState();
		}
		if (direction.getAxis() == state.getValue(BlockStateProperties.AXIS)) {
			boolean sturdy = neighborState.isFaceSturdy(level, neighborPos, direction.getOpposite(), SupportType.CENTER);
			boolean connected = neighborState.is(this) && neighborState.getValue(BlockStateProperties.AXIS) == direction.getAxis();
			if (direction.getAxisDirection() == Direction.AxisDirection.POSITIVE) {
				return state.setValue(POSITIVE_CONNECTED, sturdy || connected);
			} else {
				return state.setValue(NEGATIVE_CONNECTED, sturdy || connected);
			}
		}
		return state;
	}

	@Nullable
	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		Level level = context.getLevel();
		BlockPos pos = context.getClickedPos();
		Direction.Axis axis = context.getClickedFace().getAxis();
		Direction positive = Direction.fromAxisAndDirection(axis, Direction.AxisDirection.POSITIVE);
		Direction negative = Direction.fromAxisAndDirection(axis, Direction.AxisDirection.NEGATIVE);
		BlockPos positivePos = pos.relative(positive);
		BlockPos negativePos = pos.relative(negative);
		BlockState positiveState = level.getBlockState(positivePos);
		BlockState negativeState = level.getBlockState(negativePos);
		boolean positiveSturdy = positiveState.isFaceSturdy(level, positivePos, positive, SupportType.CENTER);
		boolean positiveConnected = positiveState.is(this) && positiveState.getValue(BlockStateProperties.AXIS) == axis;
		boolean negativeSturdy = negativeState.isFaceSturdy(level, negativePos, negative, SupportType.CENTER);
		boolean negativeConnected = negativeState.is(this) && negativeState.getValue(BlockStateProperties.AXIS) == axis;
		return this.defaultBlockState()
				.setValue(BlockStateProperties.AXIS, axis)
				.setValue(POSITIVE_CONNECTED, positiveSturdy || positiveConnected)
				.setValue(NEGATIVE_CONNECTED, negativeSturdy || negativeConnected);
	}

	@Override
	protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
		boolean flag1, flag2;
		Direction.Axis axis = state.getValue(BlockStateProperties.AXIS);

		Direction positive = Direction.get(Direction.AxisDirection.POSITIVE, axis);
		BlockPos positivePos = pos.relative(positive);
		BlockState positiveState = level.getBlockState(positivePos);
		boolean sturdy = positiveState.isFaceSturdy(level, positivePos, positive.getOpposite(), SupportType.CENTER);
		boolean connected = positiveState.is(this) && positiveState.getValue(BlockStateProperties.AXIS) == axis;
		flag1 = sturdy || connected;

		Direction negative = Direction.get(Direction.AxisDirection.NEGATIVE, axis);
		BlockPos blockpos = pos.relative(negative);
		BlockState blockstate = level.getBlockState(blockpos);
		boolean sturdy1 = blockstate.isFaceSturdy(level, blockpos, negative.getOpposite(), SupportType.CENTER);
		boolean connected1 = blockstate.is(this) && blockstate.getValue(BlockStateProperties.AXIS) == axis;
		flag2 = sturdy1 || connected1;

		return flag1 || flag2;
	}

	@Override
	public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
		return SHAPES.get(state);
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(BlockStateProperties.AXIS, POSITIVE_CONNECTED, NEGATIVE_CONNECTED);
	}

	@Override
	protected RenderShape getRenderShape(BlockState pState) {
		return RenderShape.MODEL;
	}

	@Override
	protected MapCodec<? extends BaseEntityBlock> codec() {
		return CODEC;
	}

	@Nullable
	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new InsulatorBlockEntity(pos, state);
	}
}
