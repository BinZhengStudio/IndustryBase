package net.industrybase.network.server;

import net.industrybase.world.level.block.entity.SteamEngineBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.network.CustomPayloadEvent;

public class WaterAmountPacket { // 蒸汽机水量的数据包，用于将服务端的蒸汽机水量同步到本地
	public static final StreamCodec<RegistryFriendlyByteBuf, WaterAmountPacket> STREAM_CODEC =
			StreamCodec.ofMember(WaterAmountPacket::encode, WaterAmountPacket::new);
	private final BlockPos pos;
	private final int waterAmount;

	public WaterAmountPacket(BlockPos pos, int waterAmount) {
		this.pos = pos;
		this.waterAmount = waterAmount;
	}

	public WaterAmountPacket(RegistryFriendlyByteBuf buf) {
		this.pos = buf.readBlockPos();
		this.waterAmount = buf.readInt();
	}

	public void encode(RegistryFriendlyByteBuf buf) {
		buf.writeBlockPos(this.pos);
		buf.writeInt(this.waterAmount);
	}

	public static void handler(WaterAmountPacket msg, CustomPayloadEvent.Context context) {
		context.enqueueWork(() -> {
			Level level = Minecraft.getInstance().level;
			if (level != null) {
				if (level.getBlockEntity(msg.pos) instanceof SteamEngineBlockEntity blockEntity) {
					blockEntity.setClientWaterAmount(msg.waterAmount);
				}
			}
		});
		context.setPacketHandled(true);
	}
}
