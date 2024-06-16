package net.industrybase.api.network.server;

import net.industrybase.api.electric.ElectricNetwork;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraftforge.event.network.CustomPayloadEvent;

import java.util.Optional;

public class WireConnSyncPacket {
	public static final StreamCodec<RegistryFriendlyByteBuf, WireConnSyncPacket> STREAM_CODEC =
			StreamCodec.ofMember(WireConnSyncPacket::encode, WireConnSyncPacket::new);
	private final BlockPos from;
	private final BlockPos to;
	private final boolean isRemove;

	public WireConnSyncPacket(BlockPos from, BlockPos to, boolean isRemove) {
		this.from = from;
		this.to = to;
		this.isRemove = isRemove;
	}

	public WireConnSyncPacket(RegistryFriendlyByteBuf buf) {
		this.from = buf.readBlockPos();
		this.to = buf.readBlockPos();
		this.isRemove = buf.readBoolean();
	}

	public void encode(RegistryFriendlyByteBuf buf) {
		buf.writeBlockPos(this.from);
		buf.writeBlockPos(this.to);
		buf.writeBoolean(this.isRemove);
	}

	public static void handler(WireConnSyncPacket msg, CustomPayloadEvent.Context context) {
		context.enqueueWork(() -> Optional.ofNullable(Minecraft.getInstance().level).ifPresent(level -> {
			ElectricNetwork network = ElectricNetwork.Manager.get(level);
			if (msg.isRemove) {
				network.removeClientWire(msg.from, msg.to);
			} else {
				network.addClientWire(msg.from, msg.to);
			}
		}));
		context.setPacketHandled(true);
	}
}
