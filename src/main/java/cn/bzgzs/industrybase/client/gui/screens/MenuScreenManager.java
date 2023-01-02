package cn.bzgzs.industrybase.client.gui.screens;

import cn.bzgzs.industrybase.client.gui.screens.inventory.SteamEngineScreen;
import cn.bzgzs.industrybase.world.inventory.MenuTypeList;
import net.minecraft.client.gui.screens.MenuScreens;

public class MenuScreenManager {
	public static void register() { // 将 Menu 与 Screen 进行绑定
		MenuScreens.register(MenuTypeList.STEAM_ENGINE.get(), SteamEngineScreen::new);
	}
}
