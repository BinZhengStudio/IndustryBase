package cn.bzgzs.industrybase.world.level.block;

import cn.bzgzs.industrybase.world.level.block.entity.BlockEntityTypeList;
import cn.bzgzs.industrybase.world.level.block.entity.ElectricMotorBlockEntity;
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
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public class ElectricMotorBlock extends BaseEntityBlock {
	public static final DirectionProperty FACING = BlockStateProperties.FACING;
	private static final VoxelShape X = Block.box(0.0D, 2.0D, 2.0D, 16.0D, 14.0D, 14.0D);
	private static final VoxelShape Y = Block.box(2.0D, 0.0D, 2.0D, 14.0D, 16.0D, 14.0D);
	private static final VoxelShape Z = Block.box(2.0D, 2.0D, 0.0D, 14.0D, 14.0D, 16.0D);

	public ElectricMotorBlock() {
		super(Properties.copy(Blocks.IRON_BLOCK).noOcclusion());
		this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
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
		return new ElectricMotorBlockEntity(pos, state);
	}

	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> serverType) {
		return level.isClientSide ? null : createTickerHelper(serverType, BlockEntityTypeList.ELECTRIC_MOTOR.get(), ElectricMotorBlockEntity::serverTick);
	}
}
