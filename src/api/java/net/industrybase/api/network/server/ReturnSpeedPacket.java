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

public class ReturnSpeedPacket implements CustomPacketPayload {
	public static final Type<ReturnSpeedPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(IndustryBaseApi.MODID, "return_speed"));
	public static final StreamCodec<RegistryFriendlyByteBuf, ReturnSpeedPacket> STREAM_CODEC =
			StreamCodec.composite(
					BlockPos.STREAM_CODEC,
					packet -> packet.target,
					BlockPos.STREAM_CODEC,
					packet -> packet.root,
					ByteBufCodecs.FLOAT,
					packet -> packet.speed,
					ReturnSpeedPacket::new);
	private final BlockPos target;
	private final BlockPos root;
	private final float speed;

	public ReturnSpeedPacket(BlockPos target, BlockPos root, float speed) {
		this.target = target;
		this.root = root;
		this.speed = speed;
	}

	public static void handler(ReturnSpeedPacket msg, IPayloadContext context) {
		context.enqueueWork(() ->
				TransmitNetwork.Manager.get(context.player().level()).addClientSpeed(msg.target, msg.root, msg.speed));
//		context.setPacketHandled(true);
	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
