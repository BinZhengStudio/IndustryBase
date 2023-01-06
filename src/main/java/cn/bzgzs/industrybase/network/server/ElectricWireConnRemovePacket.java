package cn.bzgzs.industrybase.network.server;

import cn.bzgzs.industrybase.api.electric.ElectricNetwork;
import cn.bzgzs.industrybase.network.CustomPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

public class ElectricWireConnRemovePacket extends CustomPacket {
	private final Map<BlockPos, Collection<BlockPos>> wireConn;

	public ElectricWireConnRemovePacket(Map<BlockPos, Collection<BlockPos>> wireConn) {
		this.wireConn = wireConn;
	}

	public ElectricWireConnRemovePacket(FriendlyByteBuf buf) {
		this.wireConn = buf.readMap(FriendlyByteBuf::readBlockPos, byteBuf -> byteBuf.readCollection((count) -> new HashSet<>(), FriendlyByteBuf::readBlockPos));
	}

	@Override
	public void encode(FriendlyByteBuf buf) {
		buf.writeMap(this.wireConn, FriendlyByteBuf::writeBlockPos, (byteBuf, set) -> byteBuf.writeCollection(set, FriendlyByteBuf::writeBlockPos));
	}

	@Override
	public void consumer(Supplier<NetworkEvent.Context> context) {
		context.get().enqueueWork(() -> Optional.ofNullable(Minecraft.getInstance().level).ifPresent(level -> ElectricNetwork.Manager.get(level).removeWireConn(this.wireConn)));
		context.get().setPacketHandled(true);
	}
}
