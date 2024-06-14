package net.industrybase.api.network.client;

import net.industrybase.api.electric.ElectricNetwork;
import net.industrybase.api.network.CustomPacket;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.Optional;
import java.util.function.Supplier;

public class UnsubscribeWireConnPacket extends CustomPacket {
	private final BlockPos target;

	public UnsubscribeWireConnPacket(BlockPos target) {
		this.target = target;
	}

	public UnsubscribeWireConnPacket(FriendlyByteBuf buf) {
		this.target = buf.readBlockPos();
	}

	@Override
	public void encode(FriendlyByteBuf buf) {
		buf.writeBlockPos(this.target);
	}

	@Override
	public void consumer(Supplier<NetworkEvent.Context> context) {
		context.get().enqueueWork(() -> Optional.ofNullable(context.get().getSender()).ifPresent(player -> ElectricNetwork.Manager.get(player.level()).unsubscribeWire(this.target, player)));
		context.get().setPacketHandled(true);
	}
}
