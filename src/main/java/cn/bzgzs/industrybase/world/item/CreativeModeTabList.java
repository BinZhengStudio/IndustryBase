package cn.bzgzs.industrybase.world.item;

import cn.bzgzs.industrybase.api.IndustryBaseApi;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class CreativeModeTabList { // 创造模式物品栏
	public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, IndustryBaseApi.MODID);
	public static final RegistryObject<CreativeModeTab> INDUSTRYBASE = CREATIVE_MODE_TABS.register("industrybase", () -> CreativeModeTab.builder()
			.title(Component.translatable("itemGroup.industrybase"))
			.icon(() -> ItemList.ELECTRIC_MOTOR.get().getDefaultInstance())
			.displayItems((parameters, output) -> { // 添加物品
				output.accept(ItemList.WIRE_COIL.get());
				output.accept(ItemList.DYNAMO.get());
				output.accept(ItemList.OAK_TRANSMISSION_ROD.get());
				output.accept(ItemList.SPRUCE_TRANSMISSION_ROD.get());
				output.accept(ItemList.BIRCH_TRANSMISSION_ROD.get());
				output.accept(ItemList.JUNGLE_TRANSMISSION_ROD.get());
				output.accept(ItemList.ACACIA_TRANSMISSION_ROD.get());
				output.accept(ItemList.DARK_OAK_TRANSMISSION_ROD.get());
				output.accept(ItemList.MANGROVE_TRANSMISSION_ROD.get());
				output.accept(ItemList.STONE_TRANSMISSION_ROD.get());
				output.accept(ItemList.IRON_TRANSMISSION_ROD.get());
				output.accept(ItemList.GOLD_TRANSMISSION_ROD.get());
				output.accept(ItemList.DIAMOND_TRANSMISSION_ROD.get());
				output.accept(ItemList.STEAM_ENGINE.get());
				output.accept(ItemList.AXIS_CONNECTOR.get());
				output.accept(ItemList.WIRE.get());
				output.accept(ItemList.WIRE_CONNECTOR.get());
				output.accept(ItemList.ELECTRIC_MOTOR.get());
			}).build());
}
