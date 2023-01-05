package cn.bzgzs.industrybase.world.item;

import cn.bzgzs.industrybase.api.electric.ConnectHelper;
import cn.bzgzs.industrybase.api.electric.IWireCoil;
import cn.bzgzs.industrybase.api.electric.IWireConnectable;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

public class WireCoilItem extends Item implements IWireCoil {
	public WireCoilItem() {
		super(new Properties().durability(100));
	}

	@Override
	public InteractionResult useOn(UseOnContext context) {
		Level level = context.getLevel();
		if (!level.isClientSide) {
			BlockPos toPos = context.getClickedPos();
			BlockEntity blockEntity = level.getBlockEntity(toPos);
			if (blockEntity instanceof IWireConnectable) {
				CompoundTag tag = context.getItemInHand().getOrCreateTag();
				if (tag.contains("ConnectPos")) {
					BlockPos fromPos = NbtUtils.readBlockPos(tag.getCompound("ConnectPos"));
					ConnectHelper.addConnect(level, fromPos, toPos, blockEntity::setChanged);
					tag.remove("ConnectPos");
				} else {
					tag.put("ConnectPos", NbtUtils.writeBlockPos(toPos));
				}
			} else {
				return InteractionResult.PASS; // TODO
			}
		}
		return InteractionResult.sidedSuccess(level.isClientSide);
	}
}
