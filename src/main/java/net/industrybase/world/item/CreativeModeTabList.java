package net.industrybase.world.item;

import net.industrybase.api.IndustryBaseApi;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class CreativeModeTabList {
	public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, IndustryBaseApi.MODID);
	public static final DeferredHolder<CreativeModeTab, CreativeModeTab> INDUSTRYBASE = CREATIVE_MODE_TABS.register("industrybase", () -> CreativeModeTab.builder()
			.title(Component.translatable("itemGroup.industrybase"))
			.icon(ItemList.ELECTRIC_MOTOR::toStack)
			.displayItems((parameters, output) -> { // 添加物品
				output.accept(ItemList.WIRE_COIL);
				output.accept(ItemList.DYNAMO);
				output.accept(ItemList.CREATIVE_DYNAMO);
				output.accept(ItemList.OAK_TRANSMISSION_ROD);
				output.accept(ItemList.SPRUCE_TRANSMISSION_ROD);
				output.accept(ItemList.BIRCH_TRANSMISSION_ROD);
				output.accept(ItemList.JUNGLE_TRANSMISSION_ROD);
				output.accept(ItemList.ACACIA_TRANSMISSION_ROD);
				output.accept(ItemList.DARK_OAK_TRANSMISSION_ROD);
				output.accept(ItemList.MANGROVE_TRANSMISSION_ROD);
				output.accept(ItemList.CHERRY_TRANSMISSION_ROD);
				output.accept(ItemList.CRIMSON_TRANSMISSION_ROD);
				output.accept(ItemList.WARPED_TRANSMISSION_ROD);
				output.accept(ItemList.STONE_TRANSMISSION_ROD);
				output.accept(ItemList.IRON_TRANSMISSION_ROD);
				output.accept(ItemList.GOLD_TRANSMISSION_ROD);
				output.accept(ItemList.DIAMOND_TRANSMISSION_ROD);
				output.accept(ItemList.STEAM_ENGINE);
				output.accept(ItemList.CREATIVE_STEAM_ENGINE);
				output.accept(ItemList.AXIS_CONNECTOR);
				output.accept(ItemList.WIRE);
				output.accept(ItemList.WIRE_CONNECTOR);
				output.accept(ItemList.ELECTRIC_MOTOR);
				output.accept(ItemList.CREATIVE_ELECTRIC_MOTOR);
				output.accept(ItemList.IRON_PIPE);
				output.accept(ItemList.WATER_PUMP);
				output.accept(ItemList.FLUID_TANK);
			}).build());
}
