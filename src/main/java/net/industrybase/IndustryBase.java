package net.industrybase;

import com.mojang.logging.LogUtils;
import net.industrybase.api.IndustryBaseApi;
import net.industrybase.world.inventory.MenuTypeList;
import net.industrybase.world.item.CreativeModeTabList;
import net.industrybase.world.item.ItemList;
import net.industrybase.world.level.block.BlockList;
import net.industrybase.world.level.block.entity.BlockEntityTypeList;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import org.slf4j.Logger;

@Mod(IndustryBaseApi.MODID)
public class IndustryBase {
	public static final Logger LOGGER = LogUtils.getLogger();

	public IndustryBase(IEventBus modEventBus, ModContainer modContainer) {
		BlockList.BLOCK.register(modEventBus);
		BlockEntityTypeList.BLOCK_ENTITY_TYPE.register(modEventBus);
		CreativeModeTabList.CREATIVE_MODE_TABS.register(modEventBus);
		MenuTypeList.MENU.register(modEventBus);
		ItemList.ITEM.register(modEventBus);

		modEventBus.addListener(this::setup);
		modEventBus.addListener(this::doClientStuff);
	}

	private void setup(final FMLCommonSetupEvent event) {
	}

	private void doClientStuff(final FMLClientSetupEvent event) {
	}
}
