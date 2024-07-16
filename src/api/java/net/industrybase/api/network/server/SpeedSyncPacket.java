package net.industrybase.api.network.server;

import net.industrybase.api.IndustryBaseApi;
import net.industrybase.api.transmit.TransmitNetwork;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class SpeedSyncPacket implements CustomPacketPayload {
	public static final Type<SpeedSyncPacket> TYPE = new Type<>(new ResourceLocation(IndustryBaseApi.MODID, "speed_sync"));
	public static final StreamCodec<RegistryFriendlyByteBuf, SpeedSyncPacket> STREAM_CODEC =
			StreamCodec.ofMember(SpeedSyncPacket::encode, SpeedSyncPacket::new);
	private final BlockPos root;
	private final float speed;

	public SpeedSyncPacket(BlockPos root, float speed) {
		this.root = root;
		this.speed = speed;
	}

	public SpeedSyncPacket(RegistryFriendlyByteBuf buf) {
		this.root = buf.readBlockPos();
		this.speed = buf.readFloat();
	}

	public void encode(RegistryFriendlyByteBuf buf) {
		buf.writeBlockPos(this.root);
		buf.writeFloat(this.speed);
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
