package net.industrybase.api.network.server;

import net.industrybase.api.IndustryBaseApi;
import net.industrybase.api.electric.ElectricNetwork;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class WireConnSyncPacket implements CustomPacketPayload {
	public static final Type<WireConnSyncPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(IndustryBaseApi.MODID, "wire_conn_sync"));
	public static final StreamCodec<RegistryFriendlyByteBuf, WireConnSyncPacket> STREAM_CODEC =
			StreamCodec.ofMember(WireConnSyncPacket::encode, WireConnSyncPacket::new);
	private final BlockPos from;
	private final BlockPos to;
	private final boolean isRemove;

	public WireConnSyncPacket(BlockPos from, BlockPos to, boolean isRemove) {
		this.from = from;
		this.to = to;
		this.isRemove = isRemove;
	}

	public WireConnSyncPacket(RegistryFriendlyByteBuf buf) {
		this.from = buf.readBlockPos();
		this.to = buf.readBlockPos();
		this.isRemove = buf.readBoolean();
	}

	public void encode(RegistryFriendlyByteBuf buf) {
		buf.writeBlockPos(this.from);
		buf.writeBlockPos(this.to);
		buf.writeBoolean(this.isRemove);
	}

	public static void handler(WireConnSyncPacket msg, IPayloadContext context) {
		context.enqueueWork(() -> {
			ElectricNetwork network = ElectricNetwork.Manager.get(context.player().level());
			if (msg.isRemove) {
				network.removeClientWire(msg.from, msg.to);
			} else {
				network.addClientWire(msg.from, msg.to);
			}
		});
//		context.setPacketHandled(true);
	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
