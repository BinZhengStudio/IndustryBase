package cn.bzgzs.industrybase.world.level.block;

import cn.bzgzs.industrybase.api.IMachine;
import cn.bzgzs.industrybase.world.level.block.entity.BlockEntityTypeList;
import cn.bzgzs.industrybase.world.level.block.entity.DynamoBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
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
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import org.jetbrains.annotations.Nullable;

public class DynamoBlock extends BaseEntityBlock implements IMachine {
	public static final DirectionProperty FACING = BlockStateProperties.FACING;

	public DynamoBlock() {
		super(Properties.copy(Blocks.IRON_BLOCK));
		this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
	}

	@Override
	@SuppressWarnings("deprecation")
	public RenderShape getRenderShape(BlockState pState) {
		return RenderShape.MODEL;
	}

	@Nullable
	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		return this.defaultBlockState().setValue(FACING, context.getNearestLookingDirection().getOpposite());
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(FACING);
	}

	@Nullable
	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new DynamoBlockEntity(pos, state);
	}

	@javax.annotation.Nullable
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntityType) {
		return createDynamoTicker(level, blockEntityType, BlockEntityTypeList.DYNAMO.get());
	}

	@Nullable
	protected static <T extends BlockEntity> BlockEntityTicker<T> createDynamoTicker(Level level, BlockEntityType<T> serverType, BlockEntityType<? extends DynamoBlockEntity> clientType) {
		return level.isClientSide ? null : createTickerHelper(serverType, clientType, DynamoBlockEntity::serverTick);
	}
}
