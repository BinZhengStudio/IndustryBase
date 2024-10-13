package net.industrybase.network.server;

import net.industrybase.api.IndustryBaseApi;
import net.industrybase.world.level.block.entity.FluidTankBlockEntity;
import net.industrybase.world.level.block.entity.SteamEngineBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class WaterAmountPayload implements CustomPacketPayload { // 蒸汽机水量的数据包，用于将服务端的蒸汽机水量同步到本地
	public static final Type<WaterAmountPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(IndustryBaseApi.MODID, "auxiliary_light_data"));
	public static final StreamCodec<RegistryFriendlyByteBuf, WaterAmountPayload> STREAM_CODEC =
			StreamCodec.composite(
					BlockPos.STREAM_CODEC,
					packet -> packet.pos,
					ByteBufCodecs.INT,
					packet -> packet.waterAmount,
					WaterAmountPayload::new);
	private final BlockPos pos;
	private final int waterAmount;

	public WaterAmountPayload(BlockPos pos, int waterAmount) {
		this.pos = pos;
		this.waterAmount = waterAmount;
	}

	public static void handler(WaterAmountPayload msg, IPayloadContext context) {
		context.enqueueWork(() -> {
			BlockEntity entity = context.player().level().getBlockEntity(msg.pos);
			if (entity instanceof SteamEngineBlockEntity blockEntity) {
				blockEntity.setClientWaterAmount(msg.waterAmount);
			} else if (entity instanceof FluidTankBlockEntity blockEntity) {
				blockEntity.setClientWaterAmount(msg.waterAmount);
			}
		});
//		context.setPacketHandled(true);
	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
