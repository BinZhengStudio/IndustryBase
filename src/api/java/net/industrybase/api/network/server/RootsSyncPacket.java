package net.industrybase.api.network.server;

import net.industrybase.api.IndustryBaseApi;
import net.industrybase.api.transmit.TransmitNetwork;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.Collection;
import java.util.HashSet;

public class RootsSyncPacket implements CustomPacketPayload {
	public static final Type<RootsSyncPacket> TYPE = new Type<>(new ResourceLocation(IndustryBaseApi.MODID, "roots_sync"));
	public static final StreamCodec<RegistryFriendlyByteBuf, RootsSyncPacket> STREAM_CODEC =
			StreamCodec.ofMember(RootsSyncPacket::encode, RootsSyncPacket::new);
	private final Collection<BlockPos> targets;
	private final BlockPos root;

	public RootsSyncPacket(Collection<BlockPos> targets, BlockPos root) {
		this.targets = targets;
		this.root = root;
	}

	public RootsSyncPacket(RegistryFriendlyByteBuf buf) {
		this.targets = buf.readCollection(count -> new HashSet<>(), RegistryFriendlyByteBuf::readBlockPos);
		this.root = buf.readBlockPos();
	}

	public void encode(RegistryFriendlyByteBuf buf) {
		buf.writeCollection(this.targets, RegistryFriendlyByteBuf::writeBlockPos);
		buf.writeBlockPos(this.root);
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
