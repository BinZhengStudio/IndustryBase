package net.industrybase.api.network.client;

import net.industrybase.api.network.ApiNetworkManager;
import net.industrybase.api.network.server.ReturnSpeedPacket;
import net.industrybase.api.transmit.TransmitNetwork;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.network.CustomPayloadEvent;
import net.minecraftforge.network.PacketDistributor;

public class SubscribeSpeedPacket {
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
	public static void handler(SubscribeSpeedPacket msg, CustomPayloadEvent.Context context) {
		context.enqueueWork(() -> {
			ServerPlayer player = context.getSender();
			if (player != null) {
				if (player.level().isAreaLoaded(msg.target, 0)) {
					TransmitNetwork network = TransmitNetwork.Manager.get(player.level());
					ApiNetworkManager.INSTANCE.send(
							new ReturnSpeedPacket(msg.target, network.root(msg.target), network.subscribeSpeed(msg.target, player)),
							PacketDistributor.PLAYER.with(player)
					);
				}
			}
		});
		context.setPacketHandled(true);
	}
}
