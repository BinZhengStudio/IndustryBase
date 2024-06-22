package net.industrybase.world.item;

import net.industrybase.api.IndustryBaseApi;
import net.industrybase.api.electric.ConnectHelper;
import net.industrybase.api.electric.IWireCoil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

public class WireCoilItem extends Item implements IWireCoil {
	public static final int MAX_LENGTH = 256;

	public WireCoilItem() {
		super(new Properties()
				.durability(MAX_LENGTH)
				.component(DataComponents.CUSTOM_DATA, CustomData.EMPTY));
	}

	@Override
	public InteractionResult useOn(UseOnContext context) {
		return ConnectHelper.wireCoilUseOn(context, MAX_LENGTH);
	}

	@SuppressWarnings("deprecation")
	@Override
	public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> components, TooltipFlag isAdvanced) {
		CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).getUnsafe();
		Optional<BlockPos> posOptional = NbtUtils.readBlockPos(tag, "ConnectPos");
		posOptional.ifPresent(bind ->
				components.add(Component.translatable("itemTooltip." + IndustryBaseApi.MODID + ".wire_coil.1",
						bind.getX(), bind.getY(), bind.getZ())));
		components.add(Component.translatable("itemTooltip." + IndustryBaseApi.MODID + ".wire_coil.2",
				stack.getMaxDamage() - stack.getDamageValue()));
	}
}
