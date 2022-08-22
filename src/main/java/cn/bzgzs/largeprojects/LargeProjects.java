package cn.bzgzs.largeprojects;

import cn.bzgzs.largeprojects.world.item.ItemList;
import cn.bzgzs.largeprojects.world.level.block.BlockList;
import com.mojang.logging.LogUtils;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

@Mod(LargeProjects.MODID)
public class LargeProjects {
	public static final String MODID = "largeprojects";
	private static final Logger LOGGER = LogUtils.getLogger();

	public LargeProjects() {
		IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
		BlockList.BLOCKS.register(modEventBus);
		ItemList.ITEMS.register(modEventBus);
		MinecraftForge.EVENT_BUS.register(this);
	}
}
