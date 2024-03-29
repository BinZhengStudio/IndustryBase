package cn.bzgzs.industrybase.api.network;

import cn.bzgzs.industrybase.api.IndustryBaseApi;
import cn.bzgzs.industrybase.api.network.client.SubscribeSpeedPacket;
import cn.bzgzs.industrybase.api.network.client.SubscribeWireConnPacket;
import cn.bzgzs.industrybase.api.network.client.UnsubscribeSpeedPacket;
import cn.bzgzs.industrybase.api.network.client.UnsubscribeWireConnPacket;
import cn.bzgzs.industrybase.api.network.server.*;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

import java.util.function.Function;

public class ApiNetworkManager {
	public static final String VERSION = "1";
	public static final SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(new ResourceLocation(IndustryBaseApi.MODID, "api"), () -> VERSION, VERSION::equals, VERSION::equals);
	private static int id = 0;

	public static void register() {
		networkThread(ReturnSpeedPacket.class, ReturnSpeedPacket::new, NetworkDirection.PLAY_TO_CLIENT);
		networkThread(SpeedSyncPacket.class, SpeedSyncPacket::new, NetworkDirection.PLAY_TO_CLIENT);
		networkThread(RootSyncPacket.class, RootSyncPacket::new, NetworkDirection.PLAY_TO_CLIENT);
		networkThread(RootsSyncPacket.class, RootsSyncPacket::new, NetworkDirection.PLAY_TO_CLIENT);
		networkThread(WireConnSyncPacket.class, WireConnSyncPacket::new, NetworkDirection.PLAY_TO_CLIENT);
		networkThread(RemoveWiresPacket.class, RemoveWiresPacket::new, NetworkDirection.PLAY_TO_CLIENT);
		networkThread(ReturnWireConnPacket.class, ReturnWireConnPacket::new, NetworkDirection.PLAY_TO_CLIENT);

		networkThread(SubscribeSpeedPacket.class, SubscribeSpeedPacket::new, NetworkDirection.PLAY_TO_SERVER);
		networkThread(UnsubscribeSpeedPacket.class, UnsubscribeSpeedPacket::new, NetworkDirection.PLAY_TO_SERVER);
		networkThread(SubscribeWireConnPacket.class, SubscribeWireConnPacket::new, NetworkDirection.PLAY_TO_SERVER);
		networkThread(UnsubscribeWireConnPacket.class, UnsubscribeWireConnPacket::new, NetworkDirection.PLAY_TO_SERVER);
	}

	private static <M extends CustomPacket> void mainThread(Class<M> packet, Function<FriendlyByteBuf, M> decoder, NetworkDirection direction) {
		INSTANCE.messageBuilder(packet, id++, direction).encoder((CustomPacket::encode)).decoder(decoder).consumerMainThread((CustomPacket::consumer)).add();
	}

	private static <M extends CustomPacket> void networkThread(Class<M> packet, Function<FriendlyByteBuf, M> decoder, NetworkDirection direction) {
		INSTANCE.messageBuilder(packet, id++, direction).encoder((CustomPacket::encode)).decoder(decoder).consumerNetworkThread((CustomPacket::consumer)).add();
	}
}
