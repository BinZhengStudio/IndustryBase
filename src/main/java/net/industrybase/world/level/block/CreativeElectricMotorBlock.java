package net.industrybase.world.level.block;

import net.industrybase.api.util.ElectricHelper;
import net.industrybase.api.util.TransmitHelper;
import net.industrybase.world.level.block.entity.CreativeElectricMotorBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public class CreativeElectricMotorBlock extends BaseEntityBlock {
	public static final DirectionProperty FACING = BlockStateProperties.FACING;
	private static final VoxelShape X = Block.box(0.0D, 2.0D, 2.0D, 16.0D, 14.0D, 14.0D);
	private static final VoxelShape Y = Block.box(2.0D, 0.0D, 2.0D, 14.0D, 16.0D, 14.0D);
	private static final VoxelShape Z = Block.box(2.0D, 2.0D, 0.0D, 14.0D, 14.0D, 16.0D);

	public CreativeElectricMotorBlock() {
		super(Properties.copy(Blocks.IRON_BLOCK).noOcclusion());
		this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
	}

	@Override
	@SuppressWarnings("deprecation")
	public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
		TransmitHelper.updateOnRemove(level, state, newState, pos);
		ElectricHelper.updateOnRemove(level, state, newState, pos);
		super.onRemove(state, level, pos, newState, isMoving);
	}

	@Override
	@SuppressWarnings("deprecation")
	public VoxelShape getShape(BlockState state, BlockGetter blockGetter, BlockPos pos, CollisionContext collisionContext) {
		return switch (state.getValue(FACING).getAxis()) {
			case X -> X;
			case Y -> Y;
			case Z -> Z;
		};
	}

	@Override
	public RenderShape getRenderShape(BlockState state) {
		return RenderShape.MODEL;
	}

	@Nullable
	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		return this.defaultBlockState().setValue(FACING, context.getClickedFace());
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(FACING);
	}

	@Nullable
	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new CreativeElectricMotorBlockEntity(pos, state);
	}
}
