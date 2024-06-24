package net.industrybase.api.transmit.network.client;

import net.industrybase.api.network.ApiNetworkManager;
import net.industrybase.api.transmit.network.server.ReturnSpeedPacket;
import net.industrybase.api.transmit.TransmitNetwork;
import net.industrybase.api.transmit.TransmitServerNetwork;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.network.CustomPayloadEvent;
import net.minecraftforge.network.PacketDistributor;

public class SpeedSubscribePacket {
	public static final StreamCodec<RegistryFriendlyByteBuf, SpeedSubscribePacket> STREAM_CODEC =
			StreamCodec.ofMember(SpeedSubscribePacket::encode, SpeedSubscribePacket::new);
	private final BlockPos clientRoot;
	private final boolean unsubscribe;

	public SpeedSubscribePacket(BlockPos clientRoot, boolean unsubscribe) {
		this.clientRoot = clientRoot;
		this.unsubscribe = unsubscribe;
	}

	public SpeedSubscribePacket(RegistryFriendlyByteBuf buf) {
		this.clientRoot = buf.readBlockPos();
		this.unsubscribe = buf.readBoolean();
	}

	public void encode(RegistryFriendlyByteBuf buf) {
		buf.writeBlockPos(this.clientRoot);
	}

	@SuppressWarnings("deprecation")
	public static void handler(SpeedSubscribePacket msg, CustomPayloadEvent.Context context) {
		context.enqueueWork(() -> {
			ServerPlayer player = context.getSender();
			if (player != null) {
				if (msg.unsubscribe) {
					TransmitNetwork.Manager.getServer(player.level()).unsubscribe(msg.clientRoot, player);
				} else {
					if (player.level().isAreaLoaded(msg.clientRoot, 0)) {
						TransmitServerNetwork network = TransmitNetwork.Manager.getServer(player.level());
						ApiNetworkManager.INSTANCE.send(
								new ReturnSpeedPacket(msg.clientRoot, network.root(msg.clientRoot), network.subscribeSpeed(msg.clientRoot, player)),
								PacketDistributor.PLAYER.with(player)
						);
					}
				}
			}
		});
		context.setPacketHandled(true);
	}
}
