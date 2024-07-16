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
			.icon(() -> ItemList.ELECTRIC_MOTOR.get().getDefaultInstance())
			.displayItems((parameters, output) -> { // 添加物品
				output.accept(ItemList.WIRE_COIL.get());
				output.accept(ItemList.DYNAMO.get());
				output.accept(ItemList.CREATIVE_DYNAMO.get());
				output.accept(ItemList.OAK_TRANSMISSION_ROD.get());
				output.accept(ItemList.SPRUCE_TRANSMISSION_ROD.get());
				output.accept(ItemList.BIRCH_TRANSMISSION_ROD.get());
				output.accept(ItemList.JUNGLE_TRANSMISSION_ROD.get());
				output.accept(ItemList.ACACIA_TRANSMISSION_ROD.get());
				output.accept(ItemList.DARK_OAK_TRANSMISSION_ROD.get());
				output.accept(ItemList.MANGROVE_TRANSMISSION_ROD.get());
				output.accept(ItemList.CHERRY_TRANSMISSION_ROD.get());
				output.accept(ItemList.CRIMSON_TRANSMISSION_ROD.get());
				output.accept(ItemList.WARPED_TRANSMISSION_ROD.get());
				output.accept(ItemList.STONE_TRANSMISSION_ROD.get());
				output.accept(ItemList.IRON_TRANSMISSION_ROD.get());
				output.accept(ItemList.GOLD_TRANSMISSION_ROD.get());
				output.accept(ItemList.DIAMOND_TRANSMISSION_ROD.get());
				output.accept(ItemList.STEAM_ENGINE.get());
				output.accept(ItemList.CREATIVE_STEAM_ENGINE.get());
				output.accept(ItemList.AXIS_CONNECTOR.get());
				output.accept(ItemList.WIRE.get());
				output.accept(ItemList.WIRE_CONNECTOR.get());
				output.accept(ItemList.ELECTRIC_MOTOR.get());
				output.accept(ItemList.CREATIVE_ELECTRIC_MOTOR.get());
				output.accept(ItemList.IRON_PIPE.get());
				output.accept(ItemList.WATER_PUMP.get());
			}).build());
}
