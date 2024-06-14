package net.industrybase.client.gui.screens;

import net.industrybase.client.gui.screens.inventory.CreativeSteamEngineScreen;
import net.industrybase.client.gui.screens.inventory.SteamEngineScreen;
import net.industrybase.world.inventory.MenuTypeList;
import net.minecraft.client.gui.screens.MenuScreens;

public class MenuScreenManager {
	public static void register() { // 将 Menu 与 Screen 进行绑定
		MenuScreens.register(MenuTypeList.STEAM_ENGINE.get(), SteamEngineScreen::new);
		MenuScreens.register(MenuTypeList.CREATIVE_STEAM_ENGINE.get(), CreativeSteamEngineScreen::new);
	}
}
