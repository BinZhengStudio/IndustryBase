package cn.bzgzs.industrybase;

import cn.bzgzs.industrybase.api.IndustryBaseApi;
import cn.bzgzs.industrybase.client.gui.screens.MenuScreenManager;
import cn.bzgzs.industrybase.network.NetworkManager;
import cn.bzgzs.industrybase.world.inventory.MenuTypeList;
import cn.bzgzs.industrybase.world.item.CreativeModeTabList;
import cn.bzgzs.industrybase.world.item.ItemList;
import cn.bzgzs.industrybase.world.level.block.BlockList;
import cn.bzgzs.industrybase.world.level.block.entity.BlockEntityTypeList;
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

		// 添加事件监听
		modBus.addListener(this::setup);
		modBus.addListener(this::doClientStuff);
	}

	private void setup(final FMLCommonSetupEvent event) { // 双端都要执行的初始化
		event.enqueueWork(NetworkManager::register); // 网络通信注册
	}

	private void doClientStuff(final FMLClientSetupEvent event) { // 与客户端相关的初始化
		event.enqueueWork(MenuScreenManager::register); // 绑定Container与Screen
	}
}
