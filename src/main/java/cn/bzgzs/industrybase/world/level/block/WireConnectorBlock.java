package cn.bzgzs.industrybase.world.level.block;

import cn.bzgzs.industrybase.world.level.block.entity.WireConnectorBlockEntity;
import com.google.common.collect.ImmutableMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
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
	public static final DirectionProperty FACING = BlockStateProperties.FACING;
	private static final VoxelShape CORE = Block.box(5.0D, 5.0D, 5.0D, 11.0D, 11.0D, 11.0D);
	private static final Map<Direction, VoxelShape> SHAPES_DIRECTION = new EnumMap<>(ImmutableMap.of(
			Direction.NORTH, Block.box(6.0D, 6.0D, 0.0D, 10.0D, 10.0D, 5.0D),
			Direction.EAST, Block.box(11.0D, 6.0D, 6.0D, 16.0D, 10.0D, 10.0D),
			Direction.SOUTH, Block.box(6.0D, 6.0D, 11.0D, 10.0D, 10.0D, 16.0D),
			Direction.WEST, Block.box(0.0D, 6.0D, 6.0D, 5.0D, 10.0D, 10.0D),
			Direction.UP, Block.box(6.0D, 11.0D, 6.0D, 10.0D, 16.0D, 10.0D),
			Direction.DOWN, Block.box(6.0D, 0.0D, 6.0D, 10.0D, 5.0D, 10.0D)));
	private static final Map<BlockState, VoxelShape> SHAPES = new HashMap<>();

	protected WireConnectorBlock() {
		super(Properties.copy(BlockList.WIRE.get()));
		this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.DOWN));

		for(BlockState state : this.getStateDefinition().getPossibleStates()) {
			SHAPES.put(state, Shapes.or(CORE, SHAPES_DIRECTION.get(state.getValue(FACING))));
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
	public RenderShape getRenderShape(BlockState state) {
		return RenderShape.MODEL;
	}

	@Nullable
	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new WireConnectorBlockEntity(pos, state);
	}
}
