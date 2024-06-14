package net.industrybase.api.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public abstract class CustomPacket {
	public abstract void encode(FriendlyByteBuf buf);
	public abstract void consumer(Supplier<NetworkEvent.Context> context);
}
