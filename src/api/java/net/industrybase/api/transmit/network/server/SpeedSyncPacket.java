package net.industrybase.api.transmit.network.server;

import net.industrybase.api.transmit.TransmitNetwork;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.network.CustomPayloadEvent;

public class SpeedSyncPacket {
	public static final StreamCodec<RegistryFriendlyByteBuf, SpeedSyncPacket> STREAM_CODEC =
			StreamCodec.ofMember(SpeedSyncPacket::encode, SpeedSyncPacket::new);
	private final BlockPos serverRoot;
	private final float speed;

	public SpeedSyncPacket(BlockPos serverRoot, float speed) {
		this.serverRoot = serverRoot;
		this.speed = speed;
	}

	public SpeedSyncPacket(RegistryFriendlyByteBuf buf) {
		this.serverRoot = buf.readBlockPos();
		this.speed = buf.readFloat();
	}

	public void encode(RegistryFriendlyByteBuf buf) {
		buf.writeBlockPos(this.serverRoot);
		buf.writeFloat(this.speed);
	}

	public static void handler(SpeedSyncPacket msg, CustomPayloadEvent.Context context) {
		context.enqueueWork(() -> {
			Level level = Minecraft.getInstance().level;
			if (level != null) TransmitNetwork.Manager.getClient(level).updateClientSpeed(msg.serverRoot, msg.speed);
		});
		context.setPacketHandled(true);
	}
}
