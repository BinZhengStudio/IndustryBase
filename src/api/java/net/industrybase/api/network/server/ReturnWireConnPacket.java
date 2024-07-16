package net.industrybase.api.network.server;

import net.industrybase.api.IndustryBaseApi;
import net.industrybase.api.electric.ElectricNetwork;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.Collection;
import java.util.HashSet;

public class ReturnWireConnPacket implements CustomPacketPayload {
	public static final Type<ReturnWireConnPacket> TYPE = new Type<>(new ResourceLocation(IndustryBaseApi.MODID, "return_wire_conn"));
	public static final StreamCodec<RegistryFriendlyByteBuf, ReturnWireConnPacket> STREAM_CODEC =
			StreamCodec.ofMember(ReturnWireConnPacket::encode, ReturnWireConnPacket::new);
	private final BlockPos target;
	private final Collection<BlockPos> wireConn;

	public ReturnWireConnPacket(BlockPos target, Collection<BlockPos> wireConn) {
		this.target = target;
		this.wireConn = wireConn;
	}

	public ReturnWireConnPacket(RegistryFriendlyByteBuf buf) {
		this.target = buf.readBlockPos();
		this.wireConn = buf.readCollection(count -> new HashSet<>(), RegistryFriendlyByteBuf::readBlockPos);
	}

	public void encode(RegistryFriendlyByteBuf buf) {
		buf.writeBlockPos(this.target);
		buf.writeCollection(this.wireConn, RegistryFriendlyByteBuf::writeBlockPos);
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
