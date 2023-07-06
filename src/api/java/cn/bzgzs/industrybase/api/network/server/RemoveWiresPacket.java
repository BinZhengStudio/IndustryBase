package cn.bzgzs.industrybase.api.network.server;

import cn.bzgzs.industrybase.api.electric.ElectricNetwork;
import cn.bzgzs.industrybase.api.network.CustomPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.Optional;
import java.util.function.Supplier;

public class RemoveWiresPacket extends CustomPacket {
	private final BlockPos from;

	public RemoveWiresPacket(BlockPos from) {
		this.from = from;
	}

	public RemoveWiresPacket(FriendlyByteBuf buf) {
		this.from = buf.readBlockPos();
	}

	@Override
	public void encode(FriendlyByteBuf buf) {
		buf.writeBlockPos(this.from);
	}

	@Override
	public void consumer(Supplier<NetworkEvent.Context> context) {
		context.get().enqueueWork(() -> Optional.ofNullable(Minecraft.getInstance().level).ifPresent(level -> ElectricNetwork.Manager.get(level).removeClientWires(this.from)));
		context.get().setPacketHandled(true);
	}
}
