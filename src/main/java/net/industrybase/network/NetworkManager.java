package net.industrybase.network;

import net.industrybase.api.IndustryBaseApi;
import net.industrybase.api.network.CustomPacket;
import net.industrybase.network.server.WaterAmountPacket;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

import java.util.function.Function;

public class NetworkManager {
	public static final String VERSION = "2";
	public static final SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(new ResourceLocation(IndustryBaseApi.MODID, "instance"), () -> VERSION, VERSION::equals, VERSION::equals);
	private static int id = 0;

	public static void register() {
		mainThread(WaterAmountPacket.class, WaterAmountPacket::new, NetworkDirection.PLAY_TO_CLIENT);
	}

	private static <M extends CustomPacket> void mainThread(Class<M> packet, Function<FriendlyByteBuf, M> decoder, NetworkDirection direction) {
		INSTANCE.messageBuilder(packet, id++, direction).encoder((CustomPacket::encode)).decoder(decoder).consumerMainThread((CustomPacket::consumer)).add();
	}

	private static <M extends CustomPacket> void networkThread(Class<M> packet, Function<FriendlyByteBuf, M> decoder, NetworkDirection direction) {
		INSTANCE.messageBuilder(packet, id++, direction).encoder((CustomPacket::encode)).decoder(decoder).consumerNetworkThread((CustomPacket::consumer)).add();
	}
}
