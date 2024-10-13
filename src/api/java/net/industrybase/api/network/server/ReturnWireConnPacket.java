package net.industrybase.api.network.server;

import net.industrybase.api.IndustryBaseApi;
import net.industrybase.api.electric.ElectricNetwork;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.ArrayList;
import java.util.Collection;

public class ReturnWireConnPacket implements CustomPacketPayload {
	public static final Type<ReturnWireConnPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(IndustryBaseApi.MODID, "return_wire_conn"));
	public static final StreamCodec<RegistryFriendlyByteBuf, ReturnWireConnPacket> STREAM_CODEC =
			StreamCodec.composite(
					BlockPos.STREAM_CODEC,
					packet -> packet.target,
					ByteBufCodecs.collection(ArrayList::new, BlockPos.STREAM_CODEC),
					packet -> packet.wireConn,
					ReturnWireConnPacket::new);
	private final BlockPos target;
	private final Collection<BlockPos> wireConn;

	public ReturnWireConnPacket(BlockPos target, Collection<BlockPos> wireConn) {
		this.target = target;
		this.wireConn = wireConn;
	}

	public static void handler(ReturnWireConnPacket msg, IPayloadContext context) {
		context.enqueueWork(() ->
				ElectricNetwork.Manager.get(context.player().level()).addClientWire(msg.target, msg.wireConn));
//		context.setPacketHandled(true);
	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
