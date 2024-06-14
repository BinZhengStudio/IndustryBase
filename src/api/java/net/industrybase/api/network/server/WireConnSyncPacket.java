package net.industrybase.api.network.server;

import net.industrybase.api.electric.ElectricNetwork;
import net.industrybase.api.network.CustomPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.Optional;
import java.util.function.Supplier;

public class WireConnSyncPacket extends CustomPacket {
	private final BlockPos from;
	private final BlockPos to;
	private final boolean isRemove;

	public WireConnSyncPacket(BlockPos from, BlockPos to, boolean isRemove) {
		this.from = from;
		this.to = to;
		this.isRemove = isRemove;
	}

	public WireConnSyncPacket(FriendlyByteBuf buf) {
		this.from = buf.readBlockPos();
		this.to = buf.readBlockPos();
		this.isRemove = buf.readBoolean();
	}

	@Override
	public void encode(FriendlyByteBuf buf) {
		buf.writeBlockPos(this.from);
		buf.writeBlockPos(this.to);
		buf.writeBoolean(this.isRemove);
	}

	@Override
	public void consumer(Supplier<NetworkEvent.Context> context) {
		context.get().enqueueWork(() -> Optional.ofNullable(Minecraft.getInstance().level).ifPresent(level -> {
			ElectricNetwork network = ElectricNetwork.Manager.get(level);
			if (this.isRemove) {
				network.removeClientWire(this.from, this.to);
			} else {
				network.addClientWire(this.from, this.to);
			}
		}));
		context.get().setPacketHandled(true);
	}
}
