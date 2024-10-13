package net.industrybase.api.network.server;

import net.industrybase.api.IndustryBaseApi;
import net.industrybase.api.transmit.TransmitNetwork;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class SpeedSyncPacket implements CustomPacketPayload {
	public static final Type<SpeedSyncPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(IndustryBaseApi.MODID, "speed_sync"));
	public static final StreamCodec<RegistryFriendlyByteBuf, SpeedSyncPacket> STREAM_CODEC =
			StreamCodec.composite(
					BlockPos.STREAM_CODEC,
					packet -> packet.root,
					ByteBufCodecs.FLOAT,
					packet -> packet.speed,
					SpeedSyncPacket::new);
	private final BlockPos root;
	private final float speed;

	public SpeedSyncPacket(BlockPos root, float speed) {
		this.root = root;
		this.speed = speed;
	}

	public static void handler(SpeedSyncPacket msg, IPayloadContext context) {
		context.enqueueWork(() ->
				TransmitNetwork.Manager.get(context.player().level()).updateClientSpeed(msg.root, msg.speed));
//		context.setPacketHandled(true);
	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
