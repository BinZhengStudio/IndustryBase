package net.industrybase.client.event;

import net.industrybase.api.IndustryBaseApi;
import net.industrybase.client.renderer.BlockEntityAsItemRenderer;
import net.industrybase.world.item.ItemList;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;

@EventBusSubscriber(modid = IndustryBaseApi.MODID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientExtensionRegister {
	@SubscribeEvent
	private static void initializeClientExtensions(RegisterClientExtensionsEvent event) {
		event.registerItem(new IClientItemExtensions() {
			@Override
			public BlockEntityWithoutLevelRenderer getCustomRenderer() {
				return BlockEntityAsItemRenderer.INSTANCE;
			}
		},
				ItemList.OAK_TRANSMISSION_ROD.get(),
				ItemList.SPRUCE_TRANSMISSION_ROD.get(),
				ItemList.BIRCH_TRANSMISSION_ROD.get(),
				ItemList.JUNGLE_TRANSMISSION_ROD.get(),
				ItemList.ACACIA_TRANSMISSION_ROD.get(),
				ItemList.DARK_OAK_TRANSMISSION_ROD.get(),
				ItemList.MANGROVE_TRANSMISSION_ROD.get(),
				ItemList.STONE_TRANSMISSION_ROD.get(),
				ItemList.IRON_TRANSMISSION_ROD.get(),
				ItemList.GOLD_TRANSMISSION_ROD.get(),
				ItemList.DIAMOND_TRANSMISSION_ROD.get(),
				ItemList.STEAM_ENGINE.get(),
				ItemList.CREATIVE_STEAM_ENGINE.get(),
				ItemList.AXIS_CONNECTOR.get(),
				ItemList.WIRE.get(),
				ItemList.WIRE_CONNECTOR.get(),
				ItemList.ELECTRIC_MOTOR.get(),
				ItemList.CREATIVE_ELECTRIC_MOTOR.get(),
				ItemList.CHERRY_TRANSMISSION_ROD.get(),
				ItemList.CRIMSON_TRANSMISSION_ROD.get(),
				ItemList.WARPED_TRANSMISSION_ROD.get());
	}
}
