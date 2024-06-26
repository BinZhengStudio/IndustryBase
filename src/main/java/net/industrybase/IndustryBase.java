package net.industrybase;

import net.industrybase.api.IndustryBaseApi;
import net.industrybase.api.network.ApiNetworkManager;
import net.industrybase.client.gui.screens.MenuScreenManager;
import net.industrybase.network.NetworkManager;
import net.industrybase.world.inventory.MenuTypeList;
import net.industrybase.world.item.CreativeModeTabList;
import net.industrybase.world.item.ItemList;
import net.industrybase.world.level.block.BlockList;
import net.industrybase.world.level.block.entity.BlockEntityTypeList;
import com.mojang.logging.LogUtils;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

@Mod(IndustryBaseApi.MODID)
public class IndustryBase {
	public static final Logger LOGGER = LogUtils.getLogger();

	public IndustryBase() {
		IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();

		// 注册事件监听
		BlockList.BLOCKS.register(modBus);
		BlockEntityTypeList.BLOCK_ENTITY_TYPES.register(modBus);
		CreativeModeTabList.CREATIVE_MODE_TABS.register(modBus);
		MenuTypeList.MENU_TYPES.register(modBus);
		ItemList.ITEMS.register(modBus);

		ApiNetworkManager.register();
		NetworkManager.register();

		// 添加事件监听
		modBus.addListener(this::setup);
		modBus.addListener(this::doClientStuff);
	}

	private void setup(final FMLCommonSetupEvent event) {
	}

	private void doClientStuff(final FMLClientSetupEvent event) {
		event.enqueueWork(MenuScreenManager::register);
	}
}
