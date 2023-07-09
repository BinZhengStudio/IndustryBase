package cn.bzgzs.industrybase.api.network.client;

import cn.bzgzs.industrybase.api.network.ApiNetworkManager;
import cn.bzgzs.industrybase.api.network.CustomPacket;
import cn.bzgzs.industrybase.api.network.server.ReturnSpeedPacket;
import cn.bzgzs.industrybase.api.transmit.TransmitNetwork;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;

import java.util.Optional;
import java.util.function.Supplier;

public class SubscribeSpeedPacket extends CustomPacket {
	private final BlockPos target;

	public SubscribeSpeedPacket(BlockPos target) {
		this.target = target;
	}

	public SubscribeSpeedPacket(FriendlyByteBuf buf) {
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
				TransmitNetwork network = TransmitNetwork.Manager.get(player.level());
				ApiNetworkManager.INSTANCE.send(PacketDistributor.PLAYER.with(() -> player),
						new ReturnSpeedPacket(this.target, network.root(this.target), network.subscribeSpeed(this.target, player)));
			}
		}));
		context.get().setPacketHandled(true);
	}
}
