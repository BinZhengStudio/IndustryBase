package net.industrybase.api.network;

import net.industrybase.api.IndustryBaseApi;
import net.industrybase.api.network.client.SubscribeSpeedPacket;
import net.industrybase.api.network.client.SubscribeWireConnPacket;
import net.industrybase.api.network.client.UnsubscribeSpeedPacket;
import net.industrybase.api.network.client.UnsubscribeWireConnPacket;
import net.industrybase.api.network.server.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;

public class ApiNetworkManager {
	private static final Logger LOGGER = LogManager.getLogger();
	private static final Marker MARKER = MarkerManager.getMarker("INDUSTRYBASE_API_NETWORK");
	public static final int VERSION = 0;
	public static final SimpleChannel INSTANCE = ChannelBuilder
			.named(new ResourceLocation(IndustryBaseApi.MODID, "api"))
			.networkProtocolVersion(VERSION)
			.simpleChannel()
			.play()
			.serverbound()
			.add(SubscribeSpeedPacket.class, SubscribeSpeedPacket.STREAM_CODEC, SubscribeSpeedPacket::handler)
			.add(UnsubscribeSpeedPacket.class, UnsubscribeSpeedPacket.STREAM_CODEC, UnsubscribeSpeedPacket::handler)
			.add(SubscribeWireConnPacket.class, SubscribeWireConnPacket.STREAM_CODEC, SubscribeWireConnPacket::handler)
			.add(UnsubscribeWireConnPacket.class, UnsubscribeWireConnPacket.STREAM_CODEC, UnsubscribeWireConnPacket::handler)
			.clientbound()
			.add(ReturnSpeedPacket.class, ReturnSpeedPacket.STREAM_CODEC, ReturnSpeedPacket::handler)
			.add(SpeedSyncPacket.class, SpeedSyncPacket.STREAM_CODEC, SpeedSyncPacket::handler)
			.add(RootSyncPacket.class, RootSyncPacket.STREAM_CODEC, RootSyncPacket::handler)
			.add(RootsSyncPacket.class, RootsSyncPacket.STREAM_CODEC, RootsSyncPacket::handler)
			.add(WireConnSyncPacket.class, WireConnSyncPacket.STREAM_CODEC, WireConnSyncPacket::handler)
			.add(RemoveWiresPacket.class, RemoveWiresPacket.STREAM_CODEC, RemoveWiresPacket::handler)
			.add(ReturnWireConnPacket.class, ReturnWireConnPacket.STREAM_CODEC, ReturnWireConnPacket::handler)
			.build();

	public static void register() {
		for (var channel : new Channel[]{INSTANCE})
			LOGGER.debug(MARKER, "Registering Network {} v{}", channel.getName(), channel.getProtocolVersion());
	}
}
