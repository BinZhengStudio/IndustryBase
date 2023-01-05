package cn.bzgzs.industrybase.world.item;

import cn.bzgzs.industrybase.api.IndustryBaseApi;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.CreativeModeTabEvent;

public class CreativeModeTabList { // 创造模式物品栏
	public static void register(CreativeModeTabEvent.Register event) {
		event.registerCreativeModeTab(new ResourceLocation(IndustryBaseApi.MODID, "industrybase"), (builder) -> {
			builder.icon(() -> new ItemStack(ItemList.ELECTRIC_MOTOR.get())).title(Component.translatable("itemGroup.industrybase"))
					.displayItems((set, output, flag) -> {
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
					});
		});
	}
}
