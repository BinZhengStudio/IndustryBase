package cn.bzgzs.largeprojects.network.server;

import cn.bzgzs.largeprojects.api.energy.TransmitNetwork;
import cn.bzgzs.largeprojects.network.CustomPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

public class TransmitInitialSpeedPacket extends CustomPacket {
	private final Map<BlockPos, Double> speedCollection;

	public TransmitInitialSpeedPacket(Map<BlockPos, Double> speedCollection) {
		this.speedCollection = speedCollection;
	}

	public TransmitInitialSpeedPacket(FriendlyByteBuf buf) {
		this.speedCollection = buf.readMap(FriendlyByteBuf::readBlockPos, FriendlyByteBuf::readDouble);
	}

	@Override
	public void encode(FriendlyByteBuf buf) {
		buf.writeMap(this.speedCollection, FriendlyByteBuf::writeBlockPos, FriendlyByteBuf::writeDouble);
	}

	@Override
	public void consumer(Supplier<NetworkEvent.Context> context) {
		context.get().enqueueWork(() -> Optional.ofNullable(Minecraft.getInstance().level).ifPresent(level -> TransmitNetwork.Factory.get(level).getSpeedCollection().putAll(this.speedCollection)));
		context.get().setPacketHandled(true);
	}
}
