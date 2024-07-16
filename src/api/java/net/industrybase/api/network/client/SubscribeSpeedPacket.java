package net.industrybase.api.network.client;

import net.industrybase.api.IndustryBaseApi;
import net.industrybase.api.network.server.ReturnSpeedPacket;
import net.industrybase.api.transmit.TransmitNetwork;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;


public class SubscribeSpeedPacket implements CustomPacketPayload {
	public static final Type<SubscribeSpeedPacket> TYPE = new Type<>(new ResourceLocation(IndustryBaseApi.MODID, "subscribe_speed"));
	public static final StreamCodec<RegistryFriendlyByteBuf, SubscribeSpeedPacket> STREAM_CODEC =
			StreamCodec.ofMember(SubscribeSpeedPacket::encode, SubscribeSpeedPacket::new);
	private final BlockPos target;

	public SubscribeSpeedPacket(BlockPos target) {
		this.target = target;
	}

	public SubscribeSpeedPacket(RegistryFriendlyByteBuf buf) {
		this.target = buf.readBlockPos();
	}

	public void encode(RegistryFriendlyByteBuf buf) {
		buf.writeBlockPos(this.target);
	}

	@SuppressWarnings("deprecation")
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
