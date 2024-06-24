package net.industrybase.api.transmit.network.server;

import net.industrybase.api.transmit.TransmitNetwork;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.network.CustomPayloadEvent;

public class ReturnSpeedPacket {
	public static final StreamCodec<RegistryFriendlyByteBuf, ReturnSpeedPacket> STREAM_CODEC =
			StreamCodec.ofMember(ReturnSpeedPacket::encode, ReturnSpeedPacket::new);
	private final BlockPos clientRoot;
	private final BlockPos serverRoot;
	private final float speed;

	public ReturnSpeedPacket(BlockPos clientRoot, BlockPos serverRoot, float speed) {
		this.clientRoot = clientRoot;
		this.serverRoot = serverRoot;
		this.speed = speed;
	}

	public ReturnSpeedPacket(RegistryFriendlyByteBuf buf) {
		this.clientRoot = buf.readBlockPos();
		this.serverRoot = buf.readBlockPos();
		this.speed = buf.readFloat();
	}

	public void encode(RegistryFriendlyByteBuf buf) {
		buf.writeBlockPos(this.clientRoot);
		buf.writeBlockPos(this.serverRoot);
		buf.writeFloat(this.speed);
	}

	public static void handler(ReturnSpeedPacket msg, CustomPayloadEvent.Context context) {
		context.enqueueWork(() -> {
			Level level = Minecraft.getInstance().level;
			if (level != null) TransmitNetwork.Manager.getClient(level).setClientRootAndSpeed(msg.clientRoot, msg.serverRoot, msg.speed);
		});
		context.setPacketHandled(true);
	}

}
