package net.industrybase.world.inventory;

import net.industrybase.api.IndustryBaseApi;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class MenuTypeList {
	public static final DeferredRegister<MenuType<?>> MENU = DeferredRegister.create(BuiltInRegistries.MENU, IndustryBaseApi.MODID);

	public static final DeferredHolder<MenuType<?>, MenuType<SteamEngineMenu>> STEAM_ENGINE = MENU.register("steam_engine", () -> new MenuType<>(SteamEngineMenu::new, FeatureFlags.DEFAULT_FLAGS));
	public static final DeferredHolder<MenuType<?>, MenuType<CreativeSteamEngineMenu>> CREATIVE_STEAM_ENGINE = MENU.register("creative_steam_engine", () -> new MenuType<>(CreativeSteamEngineMenu::new, FeatureFlags.DEFAULT_FLAGS));
}
