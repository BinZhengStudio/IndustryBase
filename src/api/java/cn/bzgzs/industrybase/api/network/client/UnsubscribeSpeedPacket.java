package cn.bzgzs.industrybase.api.network.client;

import cn.bzgzs.industrybase.api.network.CustomPacket;
import cn.bzgzs.industrybase.api.transmit.TransmitNetwork;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.Optional;
import java.util.function.Supplier;

public class UnsubscribeSpeedPacket extends CustomPacket {
	private final BlockPos target;

	public UnsubscribeSpeedPacket(BlockPos target) {
		this.target = target;
	}

	public UnsubscribeSpeedPacket(FriendlyByteBuf buf) {
		this.target = buf.readBlockPos();
	}

	@Override
	public void encode(FriendlyByteBuf buf) {
		buf.writeBlockPos(this.target);
	}

	@Override
	public void consumer(Supplier<NetworkEvent.Context> context) {
		context.get().enqueueWork(() -> Optional.ofNullable(context.get().getSender()).ifPresent(player -> TransmitNetwork.Manager.get(player.level()).unsubscribe(this.target, player)));
		context.get().setPacketHandled(true);
	}
}
