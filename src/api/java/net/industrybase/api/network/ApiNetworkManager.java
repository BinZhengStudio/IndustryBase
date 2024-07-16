package net.industrybase.api.network;

import net.industrybase.api.IndustryBaseApi;
import net.industrybase.api.network.client.SubscribeSpeedPacket;
import net.industrybase.api.network.client.SubscribeWireConnPacket;
import net.industrybase.api.network.client.UnsubscribeSpeedPacket;
import net.industrybase.api.network.client.UnsubscribeWireConnPacket;
import net.industrybase.api.network.server.*;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

@EventBusSubscriber(modid = IndustryBaseApi.MODID, bus = EventBusSubscriber.Bus.MOD)
public class ApiNetworkManager {
	public static final String VERSION = "1";

	@SubscribeEvent
	private static void register(final RegisterPayloadHandlersEvent event) {
		final PayloadRegistrar registrar = event.registrar(VERSION);

		registrar.playToServer(SubscribeSpeedPacket.TYPE, SubscribeSpeedPacket.STREAM_CODEC, SubscribeSpeedPacket::handler);
		registrar.playToServer(UnsubscribeSpeedPacket.TYPE, UnsubscribeSpeedPacket.STREAM_CODEC, UnsubscribeSpeedPacket::handler);
		registrar.playToServer(SubscribeWireConnPacket.TYPE, SubscribeWireConnPacket.STREAM_CODEC, SubscribeWireConnPacket::handler);
		registrar.playToServer(UnsubscribeWireConnPacket.TYPE, UnsubscribeWireConnPacket.STREAM_CODEC, UnsubscribeWireConnPacket::handler);

		registrar.playToClient(ReturnSpeedPacket.TYPE, ReturnSpeedPacket.STREAM_CODEC, ReturnSpeedPacket::handler);
		registrar.playToClient(SpeedSyncPacket.TYPE, SpeedSyncPacket.STREAM_CODEC, SpeedSyncPacket::handler);
		registrar.playToClient(RootSyncPacket.TYPE, RootSyncPacket.STREAM_CODEC, RootSyncPacket::handler);
		registrar.playToClient(RootsSyncPacket.TYPE, RootsSyncPacket.STREAM_CODEC, RootsSyncPacket::handler);
		registrar.playToClient(WireConnSyncPacket.TYPE, WireConnSyncPacket.STREAM_CODEC, WireConnSyncPacket::handler);
		registrar.playToClient(RemoveWiresPacket.TYPE, RemoveWiresPacket.STREAM_CODEC, RemoveWiresPacket::handler);
		registrar.playToClient(ReturnWireConnPacket.TYPE, ReturnWireConnPacket.STREAM_CODEC, ReturnWireConnPacket::handler);
	}
}
