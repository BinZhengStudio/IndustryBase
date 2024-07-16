package net.industrybase.client.event;

import net.industrybase.api.client.IndustryBaseClientApi;
import net.industrybase.client.renderer.BlockEntityAsItemRenderer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterClientReloadListenersEvent;

@EventBusSubscriber(value = Dist.CLIENT, bus = EventBusSubscriber.Bus.MOD)
public class ModBusEventHandler {
	@SubscribeEvent
	public static void registerReloadListeners(RegisterClientReloadListenersEvent event) {
		event.registerReloadListener(IndustryBaseClientApi.RENDER_UTIL);
		event.registerReloadListener(BlockEntityAsItemRenderer.INSTANCE);
	}
}
