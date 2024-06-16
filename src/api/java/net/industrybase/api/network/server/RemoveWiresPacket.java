package net.industrybase.api.network.server;

import net.industrybase.api.electric.ElectricNetwork;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.network.CustomPayloadEvent;

public class RemoveWiresPacket {
	public static final StreamCodec<RegistryFriendlyByteBuf, RemoveWiresPacket> STREAM_CODEC =
			StreamCodec.ofMember(RemoveWiresPacket::encode, RemoveWiresPacket::new);
	private final BlockPos from;

	public RemoveWiresPacket(BlockPos from) {
		this.from = from;
	}

	public RemoveWiresPacket(RegistryFriendlyByteBuf buf) {
		this.from = buf.readBlockPos();
	}

	public void encode(RegistryFriendlyByteBuf buf) {
		buf.writeBlockPos(this.from);
	}

	public static void handler(RemoveWiresPacket msg, CustomPayloadEvent.Context context) {
		context.enqueueWork(() -> {
			Level level = Minecraft.getInstance().level;
			if (level != null) ElectricNetwork.Manager.get(level).removeClientWires(msg.from);
		});
		context.setPacketHandled(true);
	}
}
