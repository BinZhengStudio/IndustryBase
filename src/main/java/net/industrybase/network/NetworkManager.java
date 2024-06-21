package net.industrybase.network;

import net.industrybase.api.IndustryBaseApi;
import net.industrybase.network.server.WaterAmountPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;

public class NetworkManager {
	private static final Logger LOGGER = LogManager.getLogger();
	private static final Marker MARKER = MarkerManager.getMarker("INDUSTRYBASE_NETWORK");
	public static final int VERSION = 1;
	public static final SimpleChannel INSTANCE = ChannelBuilder
			.named(new ResourceLocation(IndustryBaseApi.MODID, "main"))
			.networkProtocolVersion(VERSION)
			.simpleChannel()
			.play()
			.clientbound()
			.addMain(WaterAmountPacket.class, WaterAmountPacket.STREAM_CODEC, WaterAmountPacket::handler)
			.build();

	public static void register() {
		for (var channel : new Channel[]{INSTANCE})
			LOGGER.debug(MARKER, "Registering Network {} v{}", channel.getName(), channel.getProtocolVersion());
	}
}
