package net.industrybase.world.level.block;

import com.google.common.collect.ImmutableMap;
import com.mojang.serialization.MapCodec;

import net.industrybase.capability.electric.ElectricNetwork;
import net.industrybase.world.level.block.entity.WireConnectorBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.redstone.Orientation;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

import org.jspecify.annotations.Nullable;

public class WireConnectorBlock extends BaseEntityBlock implements SimpleWaterloggedBlock {
	public static final MapCodec<WireConnectorBlock> CODEC = simpleCodec(WireConnectorBlock::new);

	public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
	public static final EnumProperty<Direction> FACING = BlockStateProperties.FACING;
	private static final VoxelShape CORE = Block.box(4.0D, 4.0D, 4.0D, 12.0D, 12.0D, 12.0D);
	private static final Map<Direction, VoxelShape> SHAPES_DIRECTION = new EnumMap<>(ImmutableMap.of(
			Direction.NORTH, Block.box(4.0D, 4.0D, 0.0D, 12.0D, 12.0D, 4.0D),
			Direction.EAST, Block.box(12.0D, 4.0D, 4.0D, 16.0D, 12.0D, 12.0D),
			Direction.SOUTH, Block.box(4.0D, 4.0D, 12.0D, 12.0D, 12.0D, 16.0D),
			Direction.WEST, Block.box(0.0D, 4.0D, 4.0D, 4.0D, 12.0D, 12.0D),
			Direction.UP, Block.box(4.0D, 12.0D, 4.0D, 12.0D, 16.0D, 12.0D),
			Direction.DOWN, Block.box(4.0D, 0.0D, 4.0D, 12.0D, 4.0D, 12.0D)));
	private static final Map<BlockState, VoxelShape> SHAPES = new HashMap<>();

	protected WireConnectorBlock(Properties properties) {
		super(properties);
		this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.DOWN).setValue(WATERLOGGED, false));

		for (BlockState state : this.getStateDefinition().getPossibleStates()) {
			SHAPES.put(state, Shapes.or(CORE, SHAPES_DIRECTION.get(state.getValue(FACING))));
		}
	}

    @Override
    protected boolean propagatesSkylightDown(BlockState state) {
        return !state.getValue(WATERLOGGED);
    }

	@Override
	protected FluidState getFluidState(BlockState pState) {
		return pState.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(pState);
	}

    @Override
    protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block block,
            @Nullable Orientation orientation, boolean movedByPiston) {
        super.neighborChanged(state, level, pos, block, orientation, movedByPiston);
        if (!level.isClientSide()) {
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (blockEntity != null) {
                ElectricNetwork.Manager.get(level).addOrChangeBlock(pos, blockEntity::setChanged);
            }
        }
    }

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		FluidState fluidState = context.getLevel().getFluidState(context.getClickedPos());
		return this.defaultBlockState()
				.setValue(FACING, context.getClickedFace().getOpposite())
				.setValue(WATERLOGGED, fluidState.getType() == Fluids.WATER);
	}

	@Override
	public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
		return SHAPES.get(state);
	}

    @Override
    protected BlockState updateShape(BlockState state, LevelReader level, ScheduledTickAccess ticks, BlockPos pos,
            Direction directionToNeighbor, BlockPos neighborPos, BlockState neighborState, RandomSource random) {
		if (state.getValue(WATERLOGGED)) {
			ticks.scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(level));
		}
        return super.updateShape(state, level, ticks, pos, directionToNeighbor, neighborPos, neighborState, random);
    }

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(FACING, WATERLOGGED);
	}

	@Override
	protected MapCodec<? extends BaseEntityBlock> codec() {
		return CODEC;
	}

	@Override
	public RenderShape getRenderShape(BlockState state) {
		return RenderShape.MODEL;
	}

	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new WireConnectorBlockEntity(pos, state);
	}
}
