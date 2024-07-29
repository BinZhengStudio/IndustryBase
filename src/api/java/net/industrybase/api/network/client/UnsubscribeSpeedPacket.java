package net.industrybase.api.network.client;

import net.industrybase.api.IndustryBaseApi;
import net.industrybase.api.transmit.TransmitNetwork;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class UnsubscribeSpeedPacket implements CustomPacketPayload {
	public static final Type<UnsubscribeSpeedPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(IndustryBaseApi.MODID, "unsubscribe_speed"));
	public static final StreamCodec<RegistryFriendlyByteBuf, UnsubscribeSpeedPacket> STREAM_CODEC = StreamCodec.ofMember(UnsubscribeSpeedPacket::encode, UnsubscribeSpeedPacket::new);
	private final BlockPos target;

	public UnsubscribeSpeedPacket(BlockPos target) {
		this.target = target;
	}

	public UnsubscribeSpeedPacket(RegistryFriendlyByteBuf buf) {
		this.target = buf.readBlockPos();
	}

	public void encode(RegistryFriendlyByteBuf buf) {
		buf.writeBlockPos(this.target);
	}

	public static void handler(UnsubscribeSpeedPacket msg, IPayloadContext context) {
		context.enqueueWork(() -> {
			ServerPlayer player = (ServerPlayer) context.player();
			TransmitNetwork.Manager.get(player.level()).unsubscribe(msg.target, player);
		});
//		context.setPacketHandled(true);
	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
