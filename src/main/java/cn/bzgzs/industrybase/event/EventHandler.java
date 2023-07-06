package cn.bzgzs.industrybase.event;

import cn.bzgzs.industrybase.api.event.TransmitNetworkEvent;
import cn.bzgzs.industrybase.api.network.ApiNetworkManager;
import cn.bzgzs.industrybase.api.network.server.TransmitInitInfoPacket;
import cn.bzgzs.industrybase.api.network.server.TransmitRootSyncPacket;
import cn.bzgzs.industrybase.api.network.server.TransmitSpeedSyncPacket;
import cn.bzgzs.industrybase.api.transmit.TransmitNetwork;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;

@Mod.EventBusSubscriber
public class EventHandler {
	@SubscribeEvent
	public static void sendInitialInfoToPlayer(PlayerEvent.PlayerLoggedInEvent event) {
		if (event.getEntity() instanceof ServerPlayer player) {
			// 向新进入的玩家发送传动网络的相关数据
			TransmitNetwork transmitNetwork = TransmitNetwork.Manager.get(player.level());
			ApiNetworkManager.INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), new TransmitInitInfoPacket(transmitNetwork.getSpeedCollection(), transmitNetwork.getRootCollection()));
		}
	}

	@SubscribeEvent
	public static void syncSpeedToClient(TransmitNetworkEvent.UpdateSpeedEvent event) {
		if (event.getLevel() instanceof ServerLevel level) {
			// 向客户端同步传动网络的速度信息
			level.getPlayers(player -> true).forEach(player -> ApiNetworkManager.INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), new TransmitSpeedSyncPacket(event.getChanged(), event.getDeleted())));
		}
	}

	@SubscribeEvent
	public static void syncRootToClient(TransmitNetworkEvent.UpdateRootEvent event) {
		if (event.getLevel() instanceof ServerLevel level) {
			// 向客户端同步传动网络中心方块的信息
			level.getPlayers(player -> true).forEach(player -> ApiNetworkManager.INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), new TransmitRootSyncPacket(event.getChanged(), event.getDeleted())));
		}
	}
}
