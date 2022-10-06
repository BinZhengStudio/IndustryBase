package cn.bzgzs.industrybase.network.server;

import cn.bzgzs.industrybase.api.transmit.TransmitNetwork;
import cn.bzgzs.industrybase.network.CustomPacket;
import com.google.common.collect.Sets;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;

public class TransmitRootSyncPacket extends CustomPacket {
	private final Map<BlockPos, BlockPos> rootCollection;
	private final Set<BlockPos> deleted;

	public TransmitRootSyncPacket(Map<BlockPos, BlockPos> rootCollection, Set<BlockPos> deleted) {
		this.rootCollection = rootCollection;
		this.deleted = deleted;
	}

	public TransmitRootSyncPacket(FriendlyByteBuf buf) {
		this.rootCollection = buf.readMap(FriendlyByteBuf::readBlockPos, FriendlyByteBuf::readBlockPos);
		this.deleted = buf.readCollection(Sets::newHashSetWithExpectedSize, FriendlyByteBuf::readBlockPos);
	}

	@Override
	public void encode(FriendlyByteBuf buf) {
		buf.writeMap(this.rootCollection, FriendlyByteBuf::writeBlockPos, FriendlyByteBuf::writeBlockPos);
		buf.writeCollection(this.deleted, FriendlyByteBuf::writeBlockPos);
	}

	@Override
	public void consumer(Supplier<NetworkEvent.Context> context) {
		context.get().enqueueWork(() -> Optional.ofNullable(Minecraft.getInstance().level).ifPresent(level -> TransmitNetwork.Manager.get(level).updateRootCollection(this.rootCollection, this.deleted)));
		context.get().setPacketHandled(true);
	}
}
