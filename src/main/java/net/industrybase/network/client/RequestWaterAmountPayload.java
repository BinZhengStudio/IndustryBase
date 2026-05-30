package net.industrybase.network.client;

import net.industrybase.api.IndustryBaseApi;
import net.industrybase.network.server.WaterAmountPayload;
import net.industrybase.world.level.block.entity.FluidTankBlockEntity;
import net.industrybase.world.level.block.entity.SteamEngineBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class RequestWaterAmountPayload implements CustomPacketPayload {
	public static final CustomPacketPayload.Type<RequestWaterAmountPayload> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(IndustryBaseApi.MODID, "request_water_amount"));
	public static final StreamCodec<RegistryFriendlyByteBuf, RequestWaterAmountPayload> STREAM_CODEC =
			StreamCodec.composite(
					BlockPos.STREAM_CODEC,
					packet -> packet.target,
					RequestWaterAmountPayload::new);
	private final BlockPos target;

	public RequestWaterAmountPayload(BlockPos target) {
		this.target = target;
	}

	public static void handler(RequestWaterAmountPayload msg, IPayloadContext context) {
		context.enqueueWork(() -> {
			ServerPlayer player = (ServerPlayer) context.player();
			if (player.level().isAreaLoaded(msg.target, 0)) {
				BlockEntity entity = player.level().getBlockEntity(msg.target);
				if (entity instanceof SteamEngineBlockEntity blockEntity) {
					PacketDistributor.sendToPlayer(player, new WaterAmountPayload(msg.target, blockEntity.getFluidAmount()));
				} else if (entity instanceof FluidTankBlockEntity blockEntity) {
					PacketDistributor.sendToPlayer(player, new WaterAmountPayload(msg.target, blockEntity.getFluidAmount()));
				}
			}
		});
	}

	@Override
	public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
