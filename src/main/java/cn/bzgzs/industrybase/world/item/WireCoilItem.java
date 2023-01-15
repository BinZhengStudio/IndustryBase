package cn.bzgzs.industrybase.world.item;

import cn.bzgzs.industrybase.api.IndustryBaseApi;
import cn.bzgzs.industrybase.api.electric.ConnectHelper;
import cn.bzgzs.industrybase.api.electric.IWireCoil;
import cn.bzgzs.industrybase.api.electric.IWireConnectable;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class WireCoilItem extends Item implements IWireCoil {
	public static final int MAX_LENGTH = 256; // 最大导线长度
	public WireCoilItem() {
		super(new Properties().durability(MAX_LENGTH));
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
					Player player = context.getPlayer();
					if (player != null) {
						ItemStack stack = context.getItemInHand();
						int durability = player.getAbilities().instabuild ? MAX_LENGTH : stack.getMaxDamage() - stack.getDamageValue();
						double distSqr = fromPos.distSqr(toPos);
						if (distSqr > durability * durability) {
							player.sendSystemMessage(Component.translatable("message." + IndustryBaseApi.MODID + ".wire_coil.too_long", durability));
						} else {
							tag.remove("ConnectPos");
							if (ConnectHelper.addConnect(level, fromPos, toPos, blockEntity::setChanged)) {
								stack.hurtAndBreak((int) Math.sqrt(distSqr), player, user -> user.broadcastBreakEvent(player.getUsedItemHand()));
							}
						}
					}
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
			components.add(Component.translatable("itemTooltip." + IndustryBaseApi.MODID + ".wire_coil", bind.getX(), bind.getY(), bind.getZ()));
		}
	}
}
