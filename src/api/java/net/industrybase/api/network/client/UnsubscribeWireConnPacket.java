package net.industrybase.api.network.client;

import net.industrybase.api.IndustryBaseApi;
import net.industrybase.api.electric.ElectricNetwork;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class UnsubscribeWireConnPacket implements CustomPacketPayload {
	public static final Type<UnsubscribeWireConnPacket> TYPE = new Type<>(new ResourceLocation(IndustryBaseApi.MODID, "unsubscribe_wire_conn"));
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

	public static void handler(UnsubscribeWireConnPacket msg, IPayloadContext context) {
		context.enqueueWork(() -> {
			ServerPlayer player = (ServerPlayer) context.player();
			ElectricNetwork.Manager.get(player.level()).unsubscribeWire(msg.target, player);
		});
//		context.setPacketHandled(true);
	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
