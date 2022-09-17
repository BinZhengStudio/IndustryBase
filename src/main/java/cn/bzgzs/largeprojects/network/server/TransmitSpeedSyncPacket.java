package cn.bzgzs.largeprojects.network.server;

import cn.bzgzs.largeprojects.api.util.TransmitNetwork;
import cn.bzgzs.largeprojects.network.CustomPacket;
import com.google.common.collect.Sets;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;

public class TransmitSpeedSyncPacket extends CustomPacket {
	private final Map<BlockPos, Double> speedCollection;
	private final Set<BlockPos> deleted;

	public TransmitSpeedSyncPacket(Map<BlockPos, Double> speedCollection, Set<BlockPos> deleted) {
		this.speedCollection = speedCollection;
		this.deleted = deleted;
	}

	public TransmitSpeedSyncPacket(FriendlyByteBuf buf) {
		this.speedCollection = buf.readMap(FriendlyByteBuf::readBlockPos, FriendlyByteBuf::readDouble);
		this.deleted = buf.readCollection(Sets::newHashSetWithExpectedSize, FriendlyByteBuf::readBlockPos);
	}

	@Override
	public void encode(FriendlyByteBuf buf) {
		buf.writeMap(this.speedCollection, FriendlyByteBuf::writeBlockPos, FriendlyByteBuf::writeDouble);
		buf.writeCollection(this.deleted, FriendlyByteBuf::writeBlockPos);
	}

	@Override
	public void consumer(Supplier<NetworkEvent.Context> context) {
		context.get().enqueueWork(() -> Optional.ofNullable(Minecraft.getInstance().level).ifPresent(level -> TransmitNetwork.Manager.get(level).updateSpeedCollection(this.speedCollection, this.deleted)));
		context.get().setPacketHandled(true);
	}
}
