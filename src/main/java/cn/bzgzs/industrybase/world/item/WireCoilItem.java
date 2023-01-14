package cn.bzgzs.industrybase.world.item;

import cn.bzgzs.industrybase.api.electric.ConnectHelper;
import cn.bzgzs.industrybase.api.electric.IWireCoil;
import cn.bzgzs.industrybase.api.electric.IWireConnectable;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class WireCoilItem extends Item implements IWireCoil {
	public WireCoilItem() {
		super(new Properties().durability(100));
	}

	@Override
	public InteractionResult useOn(UseOnContext context) {
		Level level = context.getLevel();
		BlockPos toPos = context.getClickedPos();
		BlockEntity blockEntity = level.getBlockEntity(toPos);
		if (blockEntity instanceof IWireConnectable) {
			if (!level.isClientSide) {
				CompoundTag tag = context.getItemInHand().getOrCreateTag();
				if (tag.contains("ConnectPos")) {
					BlockPos fromPos = NbtUtils.readBlockPos(tag.getCompound("ConnectPos"));
					ConnectHelper.addConnect(level, fromPos, toPos, blockEntity::setChanged);
					tag.remove("ConnectPos");
				} else {
					tag.put("ConnectPos", NbtUtils.writeBlockPos(toPos));
				}
			}
			return InteractionResult.sidedSuccess(level.isClientSide);
		}
		return InteractionResult.PASS;
	}

	@Override
	public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> components, TooltipFlag isAdvanced) {
		CompoundTag tag = stack.getTag();
		if (tag != null && tag.contains("ConnectPos")) {
			BlockPos bind = NbtUtils.readBlockPos(tag.getCompound("ConnectPos"));
			components.add(Component.translatable("itemTooltip.industrybase.wire_coil", bind.getX(), bind.getY(), bind.getZ()));
		}
	}
}
