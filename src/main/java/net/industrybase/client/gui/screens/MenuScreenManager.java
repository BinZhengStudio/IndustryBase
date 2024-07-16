package net.industrybase.client.gui.screens;

import net.industrybase.api.IndustryBaseApi;
import net.industrybase.client.gui.screens.inventory.CreativeSteamEngineScreen;
import net.industrybase.client.gui.screens.inventory.SteamEngineScreen;
import net.industrybase.world.inventory.MenuTypeList;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

@EventBusSubscriber(modid = IndustryBaseApi.MODID, bus = EventBusSubscriber.Bus.MOD)
public class MenuScreenManager {
	@SubscribeEvent
	public static void registerScreens(RegisterMenuScreensEvent event) {
		event.register(MenuTypeList.STEAM_ENGINE.get(), SteamEngineScreen::new);
		event.register(MenuTypeList.CREATIVE_STEAM_ENGINE.get(), CreativeSteamEngineScreen::new);
	}
}
