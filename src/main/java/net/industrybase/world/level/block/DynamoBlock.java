package net.industrybase.world.level.block;

import com.mojang.serialization.MapCodec;
import net.industrybase.api.util.ElectricHelper;
import net.industrybase.api.util.TransmitHelper;
import net.industrybase.world.level.block.entity.BlockEntityTypeList;
import net.industrybase.world.level.block.entity.DynamoBlockEntity;
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

public class DynamoBlock extends BaseEntityBlock {
	public static final MapCodec<DynamoBlock> CODEC = simpleCodec((properties) -> new DynamoBlock());

	public static final DirectionProperty FACING = BlockStateProperties.FACING;

	public DynamoBlock() {
		super(Properties.ofFullCopy(Blocks.IRON_BLOCK));
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
	protected MapCodec<? extends BaseEntityBlock> codec() {
		return CODEC;
	}

	@Override
	public RenderShape getRenderShape(BlockState state) {
		return RenderShape.MODEL;
	}

	@Nullable
	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		return this.defaultBlockState().setValue(FACING, context.getClickedFace().getOpposite());
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

	@Nullable
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> serverType) {
		return level.isClientSide ? null : createTickerHelper(serverType, BlockEntityTypeList.DYNAMO.get(), DynamoBlockEntity::serverTick);
	}
}
