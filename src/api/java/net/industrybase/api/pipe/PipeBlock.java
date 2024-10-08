package net.industrybase.api.pipe;

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
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.capabilities.Capabilities;
import org.jetbrains.annotations.Nullable;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

public abstract class PipeBlock extends BaseEntityBlock {
	public static final EnumMap<Direction, BooleanProperty> PROPERTIES = new EnumMap<>(ImmutableMap.of(
			Direction.NORTH, BlockStateProperties.NORTH,
			Direction.EAST, BlockStateProperties.EAST,
			Direction.SOUTH, BlockStateProperties.SOUTH,
			Direction.WEST, BlockStateProperties.WEST,
			Direction.UP, BlockStateProperties.UP,
			Direction.DOWN, BlockStateProperties.DOWN
	));

	private static final VoxelShape CORE = Block.box(5.0D, 5.0D, 5.0D, 11.0D, 11.0D, 11.0D);
	private static final EnumMap<Direction, VoxelShape> SHAPES_DIRECTION = new EnumMap<>(ImmutableMap.of(
			Direction.NORTH, Block.box(5.0D, 5.0D, 0.0D, 11.0D, 11.0D, 5.0D),
			Direction.EAST, Block.box(11.0D, 5.0D, 5.0D, 16.0D, 11.0D, 11.0D),
			Direction.SOUTH, Block.box(5.0D, 5.0D, 11.0D, 11.0D, 11.0D, 16.0D),
			Direction.WEST, Block.box(0.0D, 5.0D, 5.0D, 5.0D, 11.0D, 11.0D),
			Direction.UP, Block.box(5.0D, 11.0D, 5.0D, 11.0D, 16.0D, 11.0D),
			Direction.DOWN, Block.box(5.0D, 0.0D, 5.0D, 11.0D, 5.0D, 11.0D)));
	private static final HashMap<BlockState, VoxelShape> SHAPES = new HashMap<>();

	protected PipeBlock(Properties properties) {
		super(properties);

		BlockState defaultState = this.stateDefinition.any();
		for (Direction direction : Direction.values()) {
			defaultState.setValue(PROPERTIES.get(direction), false);
		}
		this.registerDefaultState(defaultState);

		// calculate collision shapes for all states
		for (BlockState state : this.getStateDefinition().getPossibleStates()) {
			SHAPES.put(state, this.calculateShape(state, SHAPES_DIRECTION));
		}
	}

	private VoxelShape calculateShape(BlockState state, Map<Direction, VoxelShape> map) {
		VoxelShape shape = CORE;
		for (Direction direction : Direction.values()) {
			if (state.getValue(PROPERTIES.get(direction))) {
				shape = Shapes.or(shape, map.get(direction));
			}
		}
		return shape;
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
	public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
		return SHAPES.get(state);
	}

	@Override
	public BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor level, BlockPos currentPos, BlockPos neighborPos) {
		return state.setValue(PROPERTIES.get(direction), this.canConnect(level, direction.getOpposite(), neighborPos, neighborState));
	}

	private boolean canConnect(LevelAccessor level, Direction facing, BlockPos pos, BlockState state) {
		if (!state.is(this)) {
			BlockEntity blockEntity = level.getBlockEntity(pos);
			if (blockEntity != null) {
				Level level1 = blockEntity.getLevel();
				if (level1 != null) {
					return level1.getCapability(Capabilities.FluidHandler.BLOCK, pos, null, blockEntity, facing) != null;
				}
			}
			return false;
		}
		return true;
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(PROPERTIES.values().toArray(new BooleanProperty[0]));
	}

	@Override
	@SuppressWarnings("deprecation")
	public RenderShape getRenderShape(BlockState pState) {
		return RenderShape.MODEL;
	}
}
