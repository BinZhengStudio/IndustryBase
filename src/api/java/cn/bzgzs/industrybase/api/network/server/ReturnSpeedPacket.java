package cn.bzgzs.industrybase.api.network.server;

import cn.bzgzs.industrybase.api.network.CustomPacket;
import cn.bzgzs.industrybase.api.transmit.TransmitNetwork;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.Optional;
import java.util.function.Supplier;

public class ReturnSpeedPacket extends CustomPacket {
	private final BlockPos target;
	private final BlockPos root;
	private final float speed;

	public ReturnSpeedPacket(BlockPos target, BlockPos root, float speed) {
		this.target = target;
		this.root = root;
		this.speed = speed;
	}

	public ReturnSpeedPacket(FriendlyByteBuf buf) {
		this.target = buf.readBlockPos();
		this.root = buf.readBlockPos();
		this.speed = buf.readFloat();
	}

	@Override
	public void encode(FriendlyByteBuf buf) {
		buf.writeBlockPos(this.target);
		buf.writeBlockPos(this.root);
		buf.writeFloat(this.speed);
	}

	@Override
	public void consumer(Supplier<NetworkEvent.Context> context) {
		context.get().enqueueWork(() -> Optional.ofNullable(Minecraft.getInstance().level).ifPresent(level -> TransmitNetwork.Manager.get(level).addClientSpeed(this.target, this.root, this.speed)));
		context.get().setPacketHandled(true);
	}

}
