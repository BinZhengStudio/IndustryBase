package net.industrybase.api.network.server;

import net.industrybase.api.IndustryBaseApi;
import net.industrybase.api.transmit.TransmitNetwork;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class RootSyncPacket implements CustomPacketPayload {
	public static final Type<RootSyncPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(IndustryBaseApi.MODID, "root_sync"));
	public static final StreamCodec<RegistryFriendlyByteBuf, RootSyncPacket> STREAM_CODEC =
			StreamCodec.ofMember(RootSyncPacket::encode, RootSyncPacket::new);
	private final BlockPos targets;
	private final BlockPos root;

	public RootSyncPacket(BlockPos targets, BlockPos root) {
		this.targets = targets;
		this.root = root;
	}

	public RootSyncPacket(RegistryFriendlyByteBuf buf) {
		this.targets = buf.readBlockPos();
		this.root = buf.readBlockPos();
	}

	public void encode(RegistryFriendlyByteBuf buf) {
		buf.writeBlockPos(this.targets);
		buf.writeBlockPos(this.root);
	}

	public static void handler(RootSyncPacket msg, IPayloadContext context) {
		context.enqueueWork(() ->
				TransmitNetwork.Manager.get(context.player().level()).updateClientRoot(msg.targets, msg.root));
//		context.setPacketHandled(true);
	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
