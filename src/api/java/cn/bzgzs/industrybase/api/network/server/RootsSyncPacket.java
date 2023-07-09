package cn.bzgzs.industrybase.api.network.server;

import cn.bzgzs.industrybase.api.network.CustomPacket;
import cn.bzgzs.industrybase.api.transmit.TransmitNetwork;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.Collection;
import java.util.HashSet;
import java.util.Optional;
import java.util.function.Supplier;

public class RootsSyncPacket extends CustomPacket {
	private final Collection<BlockPos> targets;
	private final BlockPos root;

	public RootsSyncPacket(Collection<BlockPos> targets, BlockPos root) {
		this.targets = targets;
		this.root = root;
	}

	public RootsSyncPacket(FriendlyByteBuf buf) {
		this.targets = buf.readCollection(count -> new HashSet<>(), FriendlyByteBuf::readBlockPos);
		this.root = buf.readBlockPos();
	}

	@Override
	public void encode(FriendlyByteBuf buf) {
		buf.writeCollection(this.targets, FriendlyByteBuf::writeBlockPos);
		buf.writeBlockPos(this.root);
	}

	@Override
	public void consumer(Supplier<NetworkEvent.Context> context) {
		context.get().enqueueWork(() -> Optional.ofNullable(Minecraft.getInstance().level).ifPresent(level -> {
//			boolean flag = false;
//			for (BlockPos pos : this.targets) {
//				if (level.isLoaded(pos)) {
//					flag = true;
//					break;
//				}
//			}
			TransmitNetwork network = TransmitNetwork.Manager.get(level);
//			if (flag) {
				network.updateClientRoots(this.targets, this.root);
//				ApiNetworkManager.INSTANCE.sendToServer(new SubscribeSpeedPacket(this.root));
//			} else {
//				network.removeClientRoots(this.targets);
//			}
		}));
		context.get().setPacketHandled(true);
	}
}
