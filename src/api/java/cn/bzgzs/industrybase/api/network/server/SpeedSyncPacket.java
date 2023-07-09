package cn.bzgzs.industrybase.api.network.server;

import cn.bzgzs.industrybase.api.network.CustomPacket;
import cn.bzgzs.industrybase.api.transmit.TransmitNetwork;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.Optional;
import java.util.function.Supplier;

public class SpeedSyncPacket extends CustomPacket {
	private final BlockPos root;
	private final float speed;

	public SpeedSyncPacket(BlockPos root, float speed) {
		this.root = root;
		this.speed = speed;
	}

	public SpeedSyncPacket(FriendlyByteBuf buf) {
		this.root = buf.readBlockPos();
		this.speed = buf.readFloat();
	}

	@Override
	public void encode(FriendlyByteBuf buf) {
		buf.writeBlockPos(this.root);
		buf.writeFloat(this.speed);
	}

	@Override
	public void consumer(Supplier<NetworkEvent.Context> context) {
		context.get().enqueueWork(() -> Optional.ofNullable(Minecraft.getInstance().level).ifPresent(level -> TransmitNetwork.Manager.get(level).updateClientSpeed(this.root, this.speed)));
		context.get().setPacketHandled(true);
	}
}
