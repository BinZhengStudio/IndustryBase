package net.industrybase.network;

import net.industrybase.api.IndustryBaseApi;
import net.industrybase.network.client.RequestWaterAmountPayload;
import net.industrybase.network.client.SubscribeSpeedPacket;
import net.industrybase.network.client.SubscribeWireConnPacket;
import net.industrybase.network.client.UnsubscribeSpeedPacket;
import net.industrybase.network.client.UnsubscribeWireConnPacket;
import net.industrybase.network.server.RemoveWiresPacket;
import net.industrybase.network.server.ReturnSpeedPacket;
import net.industrybase.network.server.ReturnWireConnPacket;
import net.industrybase.network.server.RootSyncPacket;
import net.industrybase.network.server.RootsSyncPacket;
import net.industrybase.network.server.SpeedSyncPacket;
import net.industrybase.network.server.WaterAmountPayload;
import net.industrybase.network.server.WireConnSyncPacket;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

@EventBusSubscriber(modid = IndustryBaseApi.MODID)
public class NetworkManager {
	public static final String VERSION = "1";

	@SubscribeEvent
	private static void register(final RegisterPayloadHandlersEvent event) {
		final PayloadRegistrar registrar = event.registrar(VERSION);

		registrar.playToServer(RequestWaterAmountPayload.TYPE, RequestWaterAmountPayload.STREAM_CODEC, RequestWaterAmountPayload::handler);
		registrar.playToServer(SubscribeSpeedPacket.TYPE, SubscribeSpeedPacket.STREAM_CODEC, SubscribeSpeedPacket::handler);
		registrar.playToServer(UnsubscribeSpeedPacket.TYPE, UnsubscribeSpeedPacket.STREAM_CODEC, UnsubscribeSpeedPacket::handler);
		registrar.playToServer(SubscribeWireConnPacket.TYPE, SubscribeWireConnPacket.STREAM_CODEC, SubscribeWireConnPacket::handler);
		registrar.playToServer(UnsubscribeWireConnPacket.TYPE, UnsubscribeWireConnPacket.STREAM_CODEC, UnsubscribeWireConnPacket::handler);

		registrar.playToClient(WaterAmountPayload.TYPE, WaterAmountPayload.STREAM_CODEC, WaterAmountPayload::handler);
		registrar.playToClient(ReturnSpeedPacket.TYPE, ReturnSpeedPacket.STREAM_CODEC, ReturnSpeedPacket::handler);
		registrar.playToClient(SpeedSyncPacket.TYPE, SpeedSyncPacket.STREAM_CODEC, SpeedSyncPacket::handler);
		registrar.playToClient(RootSyncPacket.TYPE, RootSyncPacket.STREAM_CODEC, RootSyncPacket::handler);
		registrar.playToClient(RootsSyncPacket.TYPE, RootsSyncPacket.STREAM_CODEC, RootsSyncPacket::handler);
		registrar.playToClient(WireConnSyncPacket.TYPE, WireConnSyncPacket.STREAM_CODEC, WireConnSyncPacket::handler);
		registrar.playToClient(RemoveWiresPacket.TYPE, RemoveWiresPacket.STREAM_CODEC, RemoveWiresPacket::handler);
		registrar.playToClient(ReturnWireConnPacket.TYPE, ReturnWireConnPacket.STREAM_CODEC, ReturnWireConnPacket::handler);

    }
}
