package net.industrybase.network;

import net.industrybase.api.IndustryBaseApi;
import net.industrybase.network.server.WaterAmountPayload;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

@EventBusSubscriber(modid = IndustryBaseApi.MODID, bus = EventBusSubscriber.Bus.MOD)
public class NetworkManager {
	public static final String VERSION = "1";

	@SubscribeEvent
	private static void register(final RegisterPayloadHandlersEvent event) {
		final PayloadRegistrar registrar = event.registrar(VERSION);
		registrar.playToClient(WaterAmountPayload.TYPE, WaterAmountPayload.STREAM_CODEC, WaterAmountPayload::handler);
	}
}
