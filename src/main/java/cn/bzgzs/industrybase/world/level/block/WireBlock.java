package cn.bzgzs.industrybase.world.level.block;

import cn.bzgzs.industrybase.api.CapabilityList;
import cn.bzgzs.industrybase.world.level.block.entity.WireBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
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
import net.minecraft.world.level.material.Material;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;

public class WireBlock extends BaseEntityBlock {
	public static final Map<Direction, BooleanProperty> PROPERTIES;

	static {
		Map<Direction, BooleanProperty> map = new EnumMap<>(Direction.class);
		map.put(Direction.NORTH, BlockStateProperties.NORTH);
		map.put(Direction.EAST, BlockStateProperties.EAST);
		map.put(Direction.SOUTH, BlockStateProperties.SOUTH);
		map.put(Direction.WEST, BlockStateProperties.WEST);
		map.put(Direction.UP, BlockStateProperties.UP);
		map.put(Direction.DOWN, BlockStateProperties.DOWN);
		PROPERTIES = Collections.unmodifiableMap(map);
	}

	protected WireBlock() {
		super(Properties.of(Material.GLASS).strength(0.5F).sound(SoundType.METAL));
		this.registerDefaultState(this.stateDefinition.any());
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
