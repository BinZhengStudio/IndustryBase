package net.industrybase.api.network.server;

import net.industrybase.api.network.CustomPacket;
import net.industrybase.api.transmit.TransmitNetwork;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.Optional;
import java.util.function.Supplier;

public class RootSyncPacket extends CustomPacket {
	private final BlockPos targets;
	private final BlockPos root;

	public RootSyncPacket(BlockPos targets, BlockPos root) {
		this.targets = targets;
		this.root = root;
	}

	public RootSyncPacket(FriendlyByteBuf buf) {
		this.targets = buf.readBlockPos();
		this.root = buf.readBlockPos();
	}

	@Override
	public void encode(FriendlyByteBuf buf) {
		buf.writeBlockPos(this.targets);
		buf.writeBlockPos(this.root);
	}

	@Override
	public void consumer(Supplier<NetworkEvent.Context> context) {
		context.get().enqueueWork(() -> Optional.ofNullable(Minecraft.getInstance().level).ifPresent(level -> TransmitNetwork.Manager.get(level).updateClientRoot(this.targets, this.root)));
		context.get().setPacketHandled(true);
	}
}
