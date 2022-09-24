package cn.bzgzs.industrybase.network;

import cn.bzgzs.industrybase.IndustryBase;
import cn.bzgzs.industrybase.network.server.TransmitInitInfoPacket;
import cn.bzgzs.industrybase.network.server.TransmitRootSyncPacket;
import cn.bzgzs.industrybase.network.server.TransmitSpeedSyncPacket;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

import java.util.function.Function;

public class NetworkManager {
	public static final String VERSION = "1";
	public static final SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(new ResourceLocation(IndustryBase.MODID, "instance"), () -> VERSION, VERSION::equals, VERSION::equals);
	private static int id = 0;

	public static void register() {
		networkThread(TransmitInitInfoPacket.class, TransmitInitInfoPacket::new, NetworkDirection.PLAY_TO_CLIENT);
		networkThread(TransmitSpeedSyncPacket.class, TransmitSpeedSyncPacket::new, NetworkDirection.PLAY_TO_CLIENT);
		networkThread(TransmitRootSyncPacket.class, TransmitRootSyncPacket::new, NetworkDirection.PLAY_TO_CLIENT);
	}

	private static <M extends CustomPacket> void mainThread(Class<M> packet, Function<FriendlyByteBuf, M> decoder, NetworkDirection direction) {
		INSTANCE.messageBuilder(packet, id++, direction).encoder((CustomPacket::encode)).decoder(decoder).consumerMainThread((CustomPacket::consumer)).add();
	}

	private static <M extends CustomPacket> void networkThread(Class<M> packet, Function<FriendlyByteBuf, M> decoder, NetworkDirection direction) {
		INSTANCE.messageBuilder(packet, id++, direction).encoder((CustomPacket::encode)).decoder(decoder).consumerNetworkThread((CustomPacket::consumer)).add();
	}
}
