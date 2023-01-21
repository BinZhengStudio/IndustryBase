package cn.bzgzs.industrybase.api.electric;

import cn.bzgzs.industrybase.api.IndustryBaseApi;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.entity.BlockEntity;

public class ConnectHelper {
	@CanIgnoreReturnValue
	public static boolean addConnect(LevelAccessor level, BlockPos from, BlockPos to, Runnable callback) {
		ElectricNetwork network = ElectricNetwork.Manager.get(level);
		return network.addWire(from, to, callback);
	}

	public static InteractionResult wireCoilUseOn(UseOnContext context, int maxLength) {
		Level level = context.getLevel();
		BlockPos toPos = context.getClickedPos();
		BlockEntity blockEntity = level.getBlockEntity(toPos);
		if (blockEntity instanceof IWireConnectable) { // 检查右键的方块是否可连接
			if (!level.isClientSide) {
				CompoundTag tag = context.getItemInHand().getOrCreateTag();
				BlockPos fromPos;
				// 检查是否已绑定方块，并再次检查绑定的方块是否可连接（防止绑定的方块被替换）
				if (tag.contains("ConnectPos") && level.getBlockEntity(fromPos = NbtUtils.readBlockPos(tag.getCompound("ConnectPos"))) instanceof IWireConnectable) {
					Player player = context.getPlayer();
					if (player != null) {
						ItemStack stack = context.getItemInHand();
						// 耐久度，或者是剩余可连接长度（创造模式则为最大长度）
						int durability = player.getAbilities().instabuild ? maxLength : stack.getMaxDamage() - stack.getDamageValue();
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
				} else { // 未绑定方块，或已绑定方块已经被替换成不可连接方块
					tag.put("ConnectPos", NbtUtils.writeBlockPos(toPos)); // 绑定当前右键方块
				}
			}
			return InteractionResult.sidedSuccess(level.isClientSide);
		}
		return InteractionResult.PASS;
	}
}
