package net.industrybase.world.item;

import net.industrybase.api.IndustryBaseApi;
import net.industrybase.api.electric.ConnectHelper;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.context.UseOnContext;

import java.util.function.Consumer;

public class WireCoilItem extends Item {
	public static final int MAX_LENGTH = 256;

	public WireCoilItem(Properties properties) {
		super(properties
				.durability(MAX_LENGTH)
				.component(DataComponents.CUSTOM_DATA, CustomData.EMPTY));
	}

	@Override
	public InteractionResult useOn(UseOnContext context) {
		return ConnectHelper.wireCoilUseOn(context, MAX_LENGTH);
	}

    @Override
    public void appendHoverText(ItemStack itemStack, TooltipContext context, TooltipDisplay display,
            Consumer<Component> builder, TooltipFlag tooltipFlag) {
		var tag = itemStack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        tag.getIntArray("ConnectPos").ifPresent(bindPos -> {
            if (bindPos.length >= 3) {
                builder.accept(Component.translatable("itemTooltip." + IndustryBaseApi.MODID + ".wire_coil.1",
                        bindPos[0], bindPos[1], bindPos[2]));
            }
        });

		builder.accept(Component.translatable("itemTooltip." + IndustryBaseApi.MODID + ".wire_coil.2",
				itemStack.getMaxDamage() - itemStack.getDamageValue()));
    }
}
