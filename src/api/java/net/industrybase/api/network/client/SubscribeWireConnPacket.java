package net.industrybase.api.network.client;

import net.industrybase.api.electric.ElectricNetwork;
import net.industrybase.api.network.ApiNetworkManager;
import net.industrybase.api.network.CustomPacket;
import net.industrybase.api.network.server.ReturnWireConnPacket;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;

import java.util.Optional;
import java.util.function.Supplier;

public class SubscribeWireConnPacket extends CustomPacket {
	private final BlockPos target;

	public SubscribeWireConnPacket(BlockPos target) {
		this.target = target;
	}

	public SubscribeWireConnPacket(FriendlyByteBuf buf) {
		this.target = buf.readBlockPos();
	}

	@Override
	public void encode(FriendlyByteBuf buf) {
		buf.writeBlockPos(this.target);
	}

	@SuppressWarnings("deprecation")
	@Override
	public void consumer(Supplier<NetworkEvent.Context> context) {
		context.get().enqueueWork(() -> Optional.ofNullable(context.get().getSender()).ifPresent(player -> {
			if (player.level().isAreaLoaded(this.target, 0)) {
				ElectricNetwork network = ElectricNetwork.Manager.get(player.level());
				ApiNetworkManager.INSTANCE.send(PacketDistributor.PLAYER.with(() -> player),
						new ReturnWireConnPacket(this.target, network.subscribeWire(this.target, player)));
			}
		}));
		context.get().setPacketHandled(true);
	}
}
