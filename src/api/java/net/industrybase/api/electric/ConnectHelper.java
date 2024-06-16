package net.industrybase.api.electric;

import net.industrybase.api.IndustryBaseApi;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.Optional;

public class ConnectHelper {
	@CanIgnoreReturnValue
	public static boolean addConnect(LevelAccessor level, BlockPos from, BlockPos to, Runnable callback) {
		return ElectricNetwork.Manager.get(level).addWire(from, to, callback);
	}

	public static InteractionResult wireCoilUseOn(UseOnContext context, int maxLength) {
		Level level = context.getLevel();
		BlockPos toPos = context.getClickedPos();
		BlockEntity blockEntity = level.getBlockEntity(toPos);
		if (blockEntity instanceof IWireConnectable) { // Check whether the right-clicked block connectable
			if (!level.isClientSide) {
				boolean tagModified = false;
				ItemStack stack = context.getItemInHand();
				CustomData data = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY); // Get data
				CompoundTag tag = data.copyTag(); // Copy tag from data
				Optional<BlockPos> fromPosOptional = NbtUtils.readBlockPos(tag, "ConnectPos");
				BlockPos fromPos;
				// Check if stack bind block and if block connectable (prevent the block be replaced)
				if (fromPosOptional.isPresent() && level.getBlockEntity(fromPos = fromPosOptional.get()) instanceof IWireConnectable) {
					Player player = context.getPlayer();
					if (player != null) {
						// Get the remainder length, max length if creative mode
						int durability = player.getAbilities().instabuild ? maxLength : stack.getMaxDamage() - stack.getDamageValue();
						double distSqr = fromPos.distSqr(toPos);
						if (distSqr > durability * durability) {
							player.sendSystemMessage(Component.translatable("message." + IndustryBaseApi.MODID + ".wire_coil.too_long", durability));
						} else {
							tag.remove("ConnectPos");
							tagModified = true;
							if (ConnectHelper.addConnect(level, fromPos, toPos, blockEntity::setChanged)) {
								stack.hurtAndBreak((int) Math.sqrt(distSqr), player, LivingEntity.getSlotForHand(context.getHand()));
							}
						}
					}
				} else { // If didn't bind block, or the block was replaced with not connectable one
					tag.put("ConnectPos", NbtUtils.writeBlockPos(toPos)); // Bind the block right-clicked to
					tagModified = true;
				}
				if (tagModified) stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag)); // Save data
			}
			return InteractionResult.sidedSuccess(level.isClientSide);
		}
		return InteractionResult.PASS;
	}
}
