package net.industrybase.api.network.server;

import net.industrybase.api.IndustryBaseApi;
import net.industrybase.api.electric.ElectricNetwork;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class RemoveWiresPacket implements CustomPacketPayload {
	public static final Type<RemoveWiresPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(IndustryBaseApi.MODID, "remove_wires"));
	public static final StreamCodec<RegistryFriendlyByteBuf, RemoveWiresPacket> STREAM_CODEC =
			StreamCodec.composite(
					BlockPos.STREAM_CODEC,
					packet -> packet.from,
					RemoveWiresPacket::new);
	private final BlockPos from;

	public RemoveWiresPacket(BlockPos from) {
		this.from = from;
	}

	public static void handler(RemoveWiresPacket msg, IPayloadContext context) {
		context.enqueueWork(() -> ElectricNetwork.Manager.get(context.player().level()).removeClientWires(msg.from));
//		context.setPacketHandled(true);
	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
