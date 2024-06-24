package net.industrybase.api.transmit.network.server;

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
	private final BlockPos oldServerRoot;
	private final BlockPos newServerRoot;

	public RootSyncPacket(BlockPos oldServerRoot, BlockPos newServerRoot) {
		this.oldServerRoot = oldServerRoot;
		this.newServerRoot = newServerRoot;
	}

	public RootSyncPacket(RegistryFriendlyByteBuf buf) {
		this.oldServerRoot = buf.readBlockPos();
		this.newServerRoot = buf.readBlockPos();
	}

	public void encode(RegistryFriendlyByteBuf buf) {
		buf.writeBlockPos(this.oldServerRoot);
		buf.writeBlockPos(this.newServerRoot);
	}

	public static void handler(RootSyncPacket msg, CustomPayloadEvent.Context context) {
		context.enqueueWork(() -> {
			Level level = Minecraft.getInstance().level;
			if (level != null) TransmitNetwork.Manager.getClient(level).updateClientRoot(msg.oldServerRoot, msg.newServerRoot);
		});
		context.setPacketHandled(true);
	}
}
