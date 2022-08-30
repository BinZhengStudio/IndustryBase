package cn.bzgzs.largeprojects.network.server;

import cn.bzgzs.largeprojects.network.CustomPacket;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.Map;
import java.util.function.Supplier;

public class TransmitSpeedSyncPacket extends CustomPacket {
	private final Map<BlockPos, Double> speedCollection;

	public TransmitSpeedSyncPacket(Map<BlockPos, Double> speedCollection) {
		this.speedCollection = speedCollection;
	}

	public TransmitSpeedSyncPacket(FriendlyByteBuf buf) {
		this.speedCollection = buf.readMap(FriendlyByteBuf::readBlockPos, FriendlyByteBuf::readDouble);
	}

	@Override
	public void encode(FriendlyByteBuf buf) {
		buf.writeMap(this.speedCollection, FriendlyByteBuf::writeBlockPos, FriendlyByteBuf::writeDouble);
	}

	@Override
	public void consumer(Supplier<NetworkEvent.Context> context) {
		context.get().enqueueWork(() -> {
		});
	}
}
