package cn.bzgzs.industrybase.api.network.server;

import cn.bzgzs.industrybase.api.electric.ElectricNetwork;
import cn.bzgzs.industrybase.api.network.CustomPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.Collection;
import java.util.HashSet;
import java.util.Optional;
import java.util.function.Supplier;

public class ReturnWireConnPacket extends CustomPacket {
	private final BlockPos target;
	private final Collection<BlockPos> wireConn;

	public ReturnWireConnPacket(BlockPos target, Collection<BlockPos> wireConn) {
		this.target = target;
		this.wireConn = wireConn;
	}

	public ReturnWireConnPacket(FriendlyByteBuf buf) {
		this.target = buf.readBlockPos();
		this.wireConn = buf.readCollection(count -> new HashSet<>(), FriendlyByteBuf::readBlockPos);
	}

	@Override
	public void encode(FriendlyByteBuf buf) {
		buf.writeBlockPos(this.target);
		buf.writeCollection(this.wireConn, FriendlyByteBuf::writeBlockPos);
	}

	@Override
	public void consumer(Supplier<NetworkEvent.Context> context) {
		context.get().enqueueWork(() -> Optional.ofNullable(Minecraft.getInstance().level).ifPresent(level -> ElectricNetwork.Manager.get(level).addClientWire(this.target, this.wireConn)));
		context.get().setPacketHandled(true);
	}

}
