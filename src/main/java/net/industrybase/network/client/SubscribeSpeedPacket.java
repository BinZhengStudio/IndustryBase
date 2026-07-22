package net.industrybase.network.client;

import net.industrybase.capability.IndustryBaseApi;
import net.industrybase.capability.transmit.TransmitNetwork;
import net.industrybase.network.server.ReturnSpeedPacket;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;


public class SubscribeSpeedPacket implements CustomPacketPayload {
	public static final Type<SubscribeSpeedPacket> TYPE = new Type<>(Identifier.fromNamespaceAndPath(IndustryBaseApi.MODID, "subscribe_speed"));
	public static final StreamCodec<RegistryFriendlyByteBuf, SubscribeSpeedPacket> STREAM_CODEC =
			StreamCodec.composite(
					BlockPos.STREAM_CODEC,
					packet -> packet.target,
					SubscribeSpeedPacket::new);
	private final BlockPos target;

	public SubscribeSpeedPacket(BlockPos target) {
		this.target = target;
	}

	public static void handler(SubscribeSpeedPacket msg, IPayloadContext context) {
		context.enqueueWork(() -> {
			ServerPlayer player = (ServerPlayer) context.player();
			if (player.level().isAreaLoaded(msg.target, 0)) {
				TransmitNetwork network = TransmitNetwork.Manager.get(player.level());
				PacketDistributor.sendToPlayer(player, new ReturnSpeedPacket(msg.target, network.root(msg.target), network.subscribeSpeed(msg.target, player)));
			}
		});
//		context.setPacketHandled(true);
	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
