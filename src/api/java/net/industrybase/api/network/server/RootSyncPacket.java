package net.industrybase.api.network.server;

import net.industrybase.api.network.client.SubscribeSpeedPacket;
import net.industrybase.api.transmit.TransmitNetwork;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.network.CustomPayloadEvent;

public class RootSyncPacket {
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

	public static void handler(RootSyncPacket msg, CustomPayloadEvent.Context context) {
		context.enqueueWork(() -> {
			Level level = Minecraft.getInstance().level;
			if (level != null) TransmitNetwork.Manager.get(level).updateClientRoot(msg.targets, msg.root);
		});
		context.setPacketHandled(true);
	}
}
