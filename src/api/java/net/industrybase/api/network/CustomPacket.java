package net.industrybase.api.network;

import net.minecraft.network.FriendlyByteBuf;

public abstract class CustomPacket {
	public abstract void encode(FriendlyByteBuf buf);
}
