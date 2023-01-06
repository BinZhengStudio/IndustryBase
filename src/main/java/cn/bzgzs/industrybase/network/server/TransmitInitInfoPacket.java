package cn.bzgzs.industrybase.network.server;

import cn.bzgzs.industrybase.api.transmit.TransmitNetwork;
import cn.bzgzs.industrybase.network.CustomPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

public class TransmitInitInfoPacket extends CustomPacket {
	private final Map<BlockPos, Double> speedCollection;
	private final Map<BlockPos, BlockPos> rootCollection;

	public TransmitInitInfoPacket(Map<BlockPos, Double> speedCollection, Map<BlockPos, BlockPos> rootCollection) {
		this.speedCollection = speedCollection;
		this.rootCollection = rootCollection;
	}

	public TransmitInitInfoPacket(FriendlyByteBuf buf) {
		this.speedCollection = buf.readMap(FriendlyByteBuf::readBlockPos, FriendlyByteBuf::readDouble);
		this.rootCollection = buf.readMap(FriendlyByteBuf::readBlockPos, FriendlyByteBuf::readBlockPos);
	}

	@Override
	public void encode(FriendlyByteBuf buf) {
		buf.writeMap(this.speedCollection, FriendlyByteBuf::writeBlockPos, FriendlyByteBuf::writeDouble);
		buf.writeMap(this.rootCollection, FriendlyByteBuf::writeBlockPos, FriendlyByteBuf::writeBlockPos);
	}

	@Override
	public void consumer(Supplier<NetworkEvent.Context> context) {
		context.get().enqueueWork(() -> Optional.ofNullable(Minecraft.getInstance().level).ifPresent(level -> {
			TransmitNetwork network = TransmitNetwork.Manager.get(level);
			network.clientInitData(this.speedCollection, this.rootCollection);
		}));
		context.get().setPacketHandled(true);
	}
}
