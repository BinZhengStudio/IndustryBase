package net.industrybase.api.network.server;

import net.industrybase.api.electric.ElectricNetwork;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.network.CustomPayloadEvent;

import java.util.Collection;
import java.util.HashSet;

public class ReturnWireConnPacket {
	public static final StreamCodec<RegistryFriendlyByteBuf, ReturnWireConnPacket> STREAM_CODEC =
			StreamCodec.ofMember(ReturnWireConnPacket::encode, ReturnWireConnPacket::new);
	private final BlockPos target;
	private final Collection<BlockPos> wireConn;

	public ReturnWireConnPacket(BlockPos target, Collection<BlockPos> wireConn) {
		this.target = target;
		this.wireConn = wireConn;
	}

	public ReturnWireConnPacket(RegistryFriendlyByteBuf buf) {
		this.target = buf.readBlockPos();
		this.wireConn = buf.readCollection(count -> new HashSet<>(), RegistryFriendlyByteBuf::readBlockPos);
	}

	public void encode(RegistryFriendlyByteBuf buf) {
		buf.writeBlockPos(this.target);
		buf.writeCollection(this.wireConn, RegistryFriendlyByteBuf::writeBlockPos);
	}

	public static void handler(ReturnWireConnPacket msg, CustomPayloadEvent.Context context) {
		context.enqueueWork(() -> {
			Level level = Minecraft.getInstance().level;
			if (level != null) {
				ElectricNetwork.Manager.get(level).addClientWire(msg.target, msg.wireConn);
			}
		});
		context.setPacketHandled(true);
	}

}
