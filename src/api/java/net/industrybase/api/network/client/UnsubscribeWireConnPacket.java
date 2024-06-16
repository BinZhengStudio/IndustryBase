package net.industrybase.api.network.client;

import net.industrybase.api.electric.ElectricNetwork;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.network.CustomPayloadEvent;

public class UnsubscribeWireConnPacket {
	public static final StreamCodec<RegistryFriendlyByteBuf, UnsubscribeWireConnPacket> STREAM_CODEC =
			StreamCodec.ofMember(UnsubscribeWireConnPacket::encode, UnsubscribeWireConnPacket::new);
	private final BlockPos target;

	public UnsubscribeWireConnPacket(BlockPos target) {
		this.target = target;
	}

	public UnsubscribeWireConnPacket(RegistryFriendlyByteBuf buf) {
		this.target = buf.readBlockPos();
	}

	public void encode(RegistryFriendlyByteBuf buf) {
		buf.writeBlockPos(this.target);
	}

	public static void handler(UnsubscribeWireConnPacket msg, CustomPayloadEvent.Context context) {
		context.enqueueWork(() -> {
			ServerPlayer player = context.getSender();
			if (player != null) ElectricNetwork.Manager.get(player.level()).unsubscribeWire(msg.target, player);
		});
		context.setPacketHandled(true);
	}
}
