package cn.bzgzs.industrybase;

import cn.bzgzs.industrybase.client.gui.screens.MenuScreenManager;
import cn.bzgzs.industrybase.network.NetworkManager;
import cn.bzgzs.industrybase.world.item.ItemList;
import cn.bzgzs.industrybase.world.level.block.BlockList;
import cn.bzgzs.industrybase.world.level.block.entity.BlockEntityTypeList;
import com.mojang.logging.LogUtils;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

@Mod(IndustryBase.MODID)
public class IndustryBase {
	public static final String MODID = "industrybase";
	public static final Logger LOGGER = LogUtils.getLogger();

	public IndustryBase() {
		IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();

		BlockList.BLOCKS.register(modBus);
		BlockEntityTypeList.BLOCK_ENTITY_TYPES.register(modBus);
		ItemList.ITEMS.register(modBus);

		modBus.addListener(this::setup);
		modBus.addListener(this::doClientStuff);
		MinecraftForge.EVENT_BUS.register(this);
	}

	private void setup(final FMLCommonSetupEvent event) {
		event.enqueueWork(NetworkManager::register);
	}

	private void doClientStuff(final FMLClientSetupEvent event) { // 与客户端相关的代码
		event.enqueueWork(MenuScreenManager::register); // 绑定Container与Screen
	}
}
