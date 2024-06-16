package net.industrybase.api.network.server;

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
	private final BlockPos root;
	private final float speed;

	public SpeedSyncPacket(BlockPos root, float speed) {
		this.root = root;
		this.speed = speed;
	}

	public SpeedSyncPacket(RegistryFriendlyByteBuf buf) {
		this.root = buf.readBlockPos();
		this.speed = buf.readFloat();
	}

	public void encode(RegistryFriendlyByteBuf buf) {
		buf.writeBlockPos(this.root);
		buf.writeFloat(this.speed);
	}

	public static void handler(SpeedSyncPacket msg, CustomPayloadEvent.Context context) {
		context.enqueueWork(() -> {
			Level level = Minecraft.getInstance().level;
			if (level != null) {
				TransmitNetwork.Manager.get(level).updateClientSpeed(msg.root, msg.speed);
			}
		});
		context.setPacketHandled(true);
	}
}
