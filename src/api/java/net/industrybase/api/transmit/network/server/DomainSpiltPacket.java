package net.industrybase.api.transmit.network.server;

import net.industrybase.api.transmit.TransmitNetwork;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.network.CustomPayloadEvent;

public class DomainSpiltPacket {
	public static final StreamCodec<RegistryFriendlyByteBuf, DomainSpiltPacket> STREAM_CODEC =
			StreamCodec.ofMember(DomainSpiltPacket::encode, DomainSpiltPacket::new);
	private final BlockPos serverRoot;

	public DomainSpiltPacket(BlockPos serverRoot) {
		this.serverRoot = serverRoot;
	}

	public DomainSpiltPacket(RegistryFriendlyByteBuf buf) {
		this.serverRoot = buf.readBlockPos();
	}

	public void encode(RegistryFriendlyByteBuf buf) {
		buf.writeBlockPos(this.serverRoot);
	}

	public static void handler(DomainSpiltPacket msg, CustomPayloadEvent.Context context) {
		context.enqueueWork(() -> {
			Level level = Minecraft.getInstance().level;
			if (level != null) TransmitNetwork.Manager.getClient(level).afterServerSpilt(msg.serverRoot);
		});
		context.setPacketHandled(true);
	}
}
