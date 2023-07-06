package cn.bzgzs.industrybase.network.server;

import cn.bzgzs.industrybase.api.network.CustomPacket;
import cn.bzgzs.industrybase.world.level.block.entity.SteamEngineBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.Optional;
import java.util.function.Supplier;

public class WaterAmountPacket extends CustomPacket { // 蒸汽机水量的数据包，用于将服务端的蒸汽机水量同步到本地
	private final BlockPos pos;
	private final int waterAmount;

	public WaterAmountPacket(BlockPos pos, int waterAmount) {
		this.pos = pos;
		this.waterAmount = waterAmount;
	}

		public WaterAmountPacket(FriendlyByteBuf buf) {
		this.pos = buf.readBlockPos();
		this.waterAmount = buf.readInt();
	}

	@Override
	public void encode(FriendlyByteBuf buf) {
		buf.writeBlockPos(this.pos);
		buf.writeInt(this.waterAmount);
	}

	@Override
	public void consumer(Supplier<NetworkEvent.Context> context) {
		context.get().enqueueWork(() -> {
			Optional.ofNullable(Minecraft.getInstance().level).ifPresent(level -> {
				if (level.getBlockEntity(this.pos) instanceof SteamEngineBlockEntity blockEntity) {
					blockEntity.setClientWaterAmount(this.waterAmount);
				}
			});
		});
		context.get().setPacketHandled(true);
	}
}
