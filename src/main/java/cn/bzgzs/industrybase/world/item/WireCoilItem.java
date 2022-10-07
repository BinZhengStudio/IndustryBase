package cn.bzgzs.industrybase.world.item;

import cn.bzgzs.industrybase.api.electric.ConnectHelper;
import cn.bzgzs.industrybase.api.electric.IWireCoil;
import cn.bzgzs.industrybase.api.electric.IWireConnectable;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

public class WireCoilItem extends Item implements IWireCoil {
	public WireCoilItem() {
		super(new Properties().durability(100).tab(CreativeModeTabList.INDUSTRYBASE));
	}

	@Override
	public InteractionResult useOn(UseOnContext context) {
		Level level = context.getLevel();
		if (!level.isClientSide) {
			BlockPos toPos = context.getClickedPos();
			BlockEntity blockEntity = level.getBlockEntity(toPos);
			if (blockEntity instanceof IWireConnectable) {
				ItemStack stack = context.getItemInHand();
				int[] array = stack.getOrCreateTag().getIntArray("ConnectPos");
				if (array.length >= 3) {
					BlockPos fromPos = new BlockPos(array[0], array[1], array[2]);
					ConnectHelper.addConnect(level, fromPos, toPos, blockEntity::setChanged);
				} else {
					stack.getOrCreateTag().putIntArray("ConnectPos", new int[]{toPos.getX(), toPos.getY(), toPos.getZ()});
				}
			} else {
				return InteractionResult.PASS;
			}
		}
		return InteractionResult.sidedSuccess(level.isClientSide);
	}
}
