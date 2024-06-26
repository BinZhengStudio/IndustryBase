package net.industrybase.world.inventory;

import net.industrybase.api.IndustryBaseApi;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class MenuTypeList {
	public static final DeferredRegister<MenuType<?>> MENU_TYPES = DeferredRegister.create(ForgeRegistries.MENU_TYPES, IndustryBaseApi.MODID);
	public static final RegistryObject<MenuType<SteamEngineMenu>> STEAM_ENGINE = MENU_TYPES.register("steam_engine", () -> new MenuType<>(SteamEngineMenu::new, FeatureFlags.DEFAULT_FLAGS));
	public static final RegistryObject<MenuType<CreativeSteamEngineMenu>> CREATIVE_STEAM_ENGINE = MENU_TYPES.register("creative_steam_engine", () -> new MenuType<>(CreativeSteamEngineMenu::new, FeatureFlags.DEFAULT_FLAGS));
}
