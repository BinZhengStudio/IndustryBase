package cn.bzgzs.largeprojects.event;

import cn.bzgzs.largeprojects.api.event.TransmitNetworkEvent;
import cn.bzgzs.largeprojects.api.util.TransmitNetwork;
import cn.bzgzs.largeprojects.network.NetworkManager;
import cn.bzgzs.largeprojects.network.server.TransmitInitInfoPacket;
import cn.bzgzs.largeprojects.network.server.TransmitRootSyncPacket;
import cn.bzgzs.largeprojects.network.server.TransmitSpeedSyncPacket;
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
			TransmitNetwork network = TransmitNetwork.Manager.get(player.level);
			NetworkManager.INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), new TransmitInitInfoPacket(network.getSpeedCollection(), network.getRootCollection()));
		}
	}

	@SubscribeEvent
	public static void syncSpeedToClient(TransmitNetworkEvent.UpdateSpeedEvent event) {
		if (event.getLevel() instanceof ServerLevel level) {
			level.getPlayers(player -> true).forEach(player -> NetworkManager.INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), new TransmitSpeedSyncPacket(event.getChanged(), event.getDeleted())));
		}
	}

	@SubscribeEvent
	public static void syncRootToClient(TransmitNetworkEvent.UpdateRootEvent event) {
		if (event.getLevel() instanceof ServerLevel level) {
			level.getPlayers(player -> true).forEach(player -> NetworkManager.INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), new TransmitRootSyncPacket(event.getChanged(), event.getDeleted())));
		}
	}
}
