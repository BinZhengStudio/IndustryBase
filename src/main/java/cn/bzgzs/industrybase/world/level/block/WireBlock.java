package cn.bzgzs.industrybase.world.level.block;

import cn.bzgzs.industrybase.world.level.block.entity.WireBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;
import org.jetbrains.annotations.Nullable;

public class WireBlock extends BaseEntityBlock {
	protected WireBlock() {
		super(Properties.of(Material.GLASS).strength(0.5F).sound(SoundType.METAL));
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
