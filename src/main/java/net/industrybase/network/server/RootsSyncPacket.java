package net.industrybase.network.server;

import net.industrybase.capability.IndustryBaseApi;
import net.industrybase.capability.transmit.TransmitNetwork;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.ArrayList;

public class RootsSyncPacket implements CustomPacketPayload {
	public static final Type<RootsSyncPacket> TYPE = new Type<>(Identifier.fromNamespaceAndPath(IndustryBaseApi.MODID, "roots_sync"));
	public static final StreamCodec<RegistryFriendlyByteBuf, RootsSyncPacket> STREAM_CODEC =
			StreamCodec.composite(
					ByteBufCodecs.collection(ArrayList::new, BlockPos.STREAM_CODEC),
					packet -> packet.targets,
					BlockPos.STREAM_CODEC,
					packet -> packet.root, RootsSyncPacket::new);
	private final ArrayList<BlockPos> targets;
	private final BlockPos root;

	public RootsSyncPacket(ArrayList<BlockPos> targets, BlockPos root) {
		this.targets = targets;
		this.root = root;
	}

	public static void handler(RootsSyncPacket msg, IPayloadContext context) {
		context.enqueueWork(() ->
				TransmitNetwork.Manager.get(context.player().level()).updateClientRoots(msg.targets, msg.root));
//		context.setPacketHandled(true);
	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
