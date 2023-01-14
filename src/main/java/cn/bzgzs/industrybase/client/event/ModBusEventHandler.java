package cn.bzgzs.industrybase.client.event;

import cn.bzgzs.industrybase.api.client.IndustryBaseClientApi;
import cn.bzgzs.industrybase.client.renderer.BlockEntityAsItemRenderer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterClientReloadListenersEvent;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModBusEventHandler {
	@SubscribeEvent
	public static void registerReloadListeners(RegisterClientReloadListenersEvent event) {
		event.registerReloadListener(IndustryBaseClientApi.RENDER_UTIL);
		event.registerReloadListener(BlockEntityAsItemRenderer.INSTANCE);
	}
}
