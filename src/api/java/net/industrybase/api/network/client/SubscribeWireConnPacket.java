package net.industrybase.api.network.client;

import net.industrybase.api.electric.ElectricNetwork;
import net.industrybase.api.network.ApiNetworkManager;
import net.industrybase.api.network.server.ReturnWireConnPacket;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.network.CustomPayloadEvent;
import net.minecraftforge.network.PacketDistributor;

public class SubscribeWireConnPacket {
	public static final StreamCodec<RegistryFriendlyByteBuf, SubscribeWireConnPacket> STREAM_CODEC =
			StreamCodec.ofMember(SubscribeWireConnPacket::encode, SubscribeWireConnPacket::new);
	private final BlockPos target;

	public SubscribeWireConnPacket(BlockPos target) {
		this.target = target;
	}

	public SubscribeWireConnPacket(RegistryFriendlyByteBuf buf) {
		this.target = buf.readBlockPos();
	}

	public void encode(RegistryFriendlyByteBuf buf) {
		buf.writeBlockPos(this.target);
	}

	@SuppressWarnings("deprecation")
	public static void handler(SubscribeWireConnPacket msg, CustomPayloadEvent.Context context) {
		context.enqueueWork(() -> {
			ServerPlayer player = context.getSender();
			if (player != null) {
				if (player.level().isAreaLoaded(msg.target, 0)) {
					ElectricNetwork network = ElectricNetwork.Manager.get(player.level());
					ApiNetworkManager.INSTANCE.send(
							new ReturnWireConnPacket(msg.target, network.subscribeWire(msg.target, player)),
							PacketDistributor.PLAYER.with(player)
					);
				}
			}
		});
		context.setPacketHandled(true);
	}
}
