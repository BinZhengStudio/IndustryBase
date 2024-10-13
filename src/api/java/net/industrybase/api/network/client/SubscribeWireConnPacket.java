package net.industrybase.api.network.client;

import net.industrybase.api.IndustryBaseApi;
import net.industrybase.api.electric.ElectricNetwork;
import net.industrybase.api.network.server.ReturnWireConnPacket;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class SubscribeWireConnPacket implements CustomPacketPayload {
	public static final Type<SubscribeWireConnPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(IndustryBaseApi.MODID, "subscribe_wire_conn"));
	public static final StreamCodec<RegistryFriendlyByteBuf, SubscribeWireConnPacket> STREAM_CODEC =
			StreamCodec.composite(
					BlockPos.STREAM_CODEC,
					packet -> packet.target,
					SubscribeWireConnPacket::new);
	private final BlockPos target;

	public SubscribeWireConnPacket(BlockPos target) {
		this.target = target;
	}

	@SuppressWarnings("deprecation")
	public static void handler(SubscribeWireConnPacket msg, IPayloadContext context) {
		context.enqueueWork(() -> {
			ServerPlayer player = (ServerPlayer) context.player();
			if (player.level().isAreaLoaded(msg.target, 0)) {
				ElectricNetwork network = ElectricNetwork.Manager.get(player.level());
				PacketDistributor.sendToPlayer(player, new ReturnWireConnPacket(msg.target, network.subscribeWire(msg.target, player)));
			}
		});
//		context.setPacketHandled(true);
	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
