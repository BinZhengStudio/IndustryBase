package net.industrybase.api.network.server;

import net.industrybase.api.transmit.TransmitNetwork;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.network.CustomPayloadEvent;

import java.util.Collection;
import java.util.HashSet;

public class RootsSyncPacket {
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

	public static void handler(RootsSyncPacket msg, CustomPayloadEvent.Context context) {
		context.enqueueWork(() -> {
			Level level = Minecraft.getInstance().level;
			if (level != null) TransmitNetwork.Manager.get(level).updateClientRoots(msg.targets, msg.root);
		});
		context.setPacketHandled(true);
	}
}
