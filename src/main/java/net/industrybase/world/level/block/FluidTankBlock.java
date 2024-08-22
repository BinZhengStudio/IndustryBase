package net.industrybase.world.level.block;

import com.mojang.serialization.MapCodec;
import net.industrybase.world.level.block.entity.FluidTankBlockEntity;
import net.industrybase.world.level.block.entity.SteamEngineBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import org.jetbrains.annotations.Nullable;

public class FluidTankBlock extends BaseEntityBlock {
	public static final MapCodec<IronPipeBlock> CODEC = simpleCodec((properties) -> new IronPipeBlock());

	protected FluidTankBlock() {
		super(Properties.ofFullCopy(Blocks.GLASS));
	}

	@Override
	public ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
		if (level.isClientSide) {
			return ItemInteractionResult.SUCCESS;
		} else {
			if (stack.is(Items.WATER_BUCKET)) {
				BlockEntity blockEntity = level.getBlockEntity(pos);
				IFluidHandler tank = level.getCapability(Capabilities.FluidHandler.BLOCK, pos, state, blockEntity, hitResult.getDirection());
				if (tank != null) {
					tank.fill(new FluidStack(Fluids.WATER, FluidType.BUCKET_VOLUME), IFluidHandler.FluidAction.EXECUTE);
					if (!player.getAbilities().instabuild) player.setItemInHand(hand, new ItemStack(Items.BUCKET));
				}
				return ItemInteractionResult.CONSUME;
			}
			return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
		}
	}

	@Override
	protected MapCodec<? extends BaseEntityBlock> codec() {
		return CODEC;
	}

	@Nullable
	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new FluidTankBlockEntity(pos, state);
	}

	@Override
	protected RenderShape getRenderShape(BlockState pState) {
		return RenderShape.ENTITYBLOCK_ANIMATED;
	}
}
