package net.industrybase.world.level.block;

import com.mojang.serialization.MapCodec;
import net.industrybase.world.level.block.entity.WaterPumpBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class WaterPumpBlock extends BaseEntityBlock {
	public static final MapCodec<WaterPumpBlock> CODEC = simpleCodec((properties) -> new WaterPumpBlock());

	protected WaterPumpBlock() {
		super(Properties.ofFullCopy(BlockList.DYNAMO.get()));
	}

	@Override
	protected MapCodec<? extends BaseEntityBlock> codec() {
		return CODEC;
	}

	@Nullable
	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new WaterPumpBlockEntity(pos, state);
	}
}
