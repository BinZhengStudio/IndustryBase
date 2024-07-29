package net.industrybase.api.network.server;

import net.industrybase.api.IndustryBaseApi;
import net.industrybase.api.transmit.TransmitNetwork;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class ReturnSpeedPacket implements CustomPacketPayload {
	public static final Type<ReturnSpeedPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(IndustryBaseApi.MODID, "return_speed"));
	public static final StreamCodec<RegistryFriendlyByteBuf, ReturnSpeedPacket> STREAM_CODEC =
			StreamCodec.ofMember(ReturnSpeedPacket::encode, ReturnSpeedPacket::new);
	private final BlockPos target;
	private final BlockPos root;
	private final float speed;

	public ReturnSpeedPacket(BlockPos target, BlockPos root, float speed) {
		this.target = target;
		this.root = root;
		this.speed = speed;
	}

	public ReturnSpeedPacket(RegistryFriendlyByteBuf buf) {
		this.target = buf.readBlockPos();
		this.root = buf.readBlockPos();
		this.speed = buf.readFloat();
	}

	public void encode(RegistryFriendlyByteBuf buf) {
		buf.writeBlockPos(this.target);
		buf.writeBlockPos(this.root);
		buf.writeFloat(this.speed);
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
