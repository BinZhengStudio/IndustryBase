package net.industrybase.api.network.client;

import net.industrybase.api.transmit.TransmitNetwork;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.network.CustomPayloadEvent;

public class UnsubscribeSpeedPacket {
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

	public static void handler(UnsubscribeSpeedPacket msg, CustomPayloadEvent.Context context) {
		context.enqueueWork(() -> {
			ServerPlayer player = context.getSender();
			if (player != null) TransmitNetwork.Manager.get(player.level()).unsubscribe(msg.target, player);
		});
		context.setPacketHandled(true);
	}
}
