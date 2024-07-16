package net.industrybase.api.transmit;

import net.industrybase.api.util.TransmitHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public abstract class TransmissionRodBlock extends BaseEntityBlock {
	private static final VoxelShape X = Block.box(0.0D, 5.0D, 5.0D, 16.0D, 11.0D, 11.0D);
	private static final VoxelShape Y = Block.box(5.0D, 0.0D, 5.0D, 11.0D, 16.0D, 11.0D);
	private static final VoxelShape Z = Block.box(5.0D, 5.0D, 0.0D, 11.0D, 11.0D, 16.0D);
	private final int maxResistance;

	public TransmissionRodBlock(Properties properties, int maxResistance) {
		super(properties.noOcclusion().randomTicks());
		this.maxResistance = maxResistance;
		this.registerDefaultState(this.stateDefinition.any().setValue(BlockStateProperties.AXIS, Direction.Axis.X));
	}

	@Override
	public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
		TransmitHelper.updateOnRemove(level, state, newState, pos);
		super.onRemove(state, level, pos, newState, isMoving);
	}

	@Override
	public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
		if (!level.isClientSide) {
			TransmitNetwork network = TransmitNetwork.Manager.get(level);
			if (network.speed(pos) > 0.0D && ((double) network.totalResistance(pos) / network.size(pos)) > this.maxResistance) {
				level.destroyBlock(pos, true);
			}
		}
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		return this.defaultBlockState().setValue(BlockStateProperties.AXIS, context.getClickedFace().getAxis());
	}

	@Override
	public VoxelShape getShape(BlockState state, BlockGetter blockGetter, BlockPos pos, CollisionContext collisionContext) {
		return switch (state.getValue(BlockStateProperties.AXIS)) {
			case X -> X;
			case Y -> Y;
			case Z -> Z;
		};
	}

	@SuppressWarnings("deprecation")
	@Override
	public RenderShape getRenderShape(BlockState state) {
		return RenderShape.ENTITYBLOCK_ANIMATED;
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(BlockStateProperties.AXIS);
	}
}
