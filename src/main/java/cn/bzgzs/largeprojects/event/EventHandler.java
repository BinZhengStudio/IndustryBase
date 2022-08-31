package cn.bzgzs.largeprojects.event;

import cn.bzgzs.largeprojects.api.energy.TransmitNetwork;
import cn.bzgzs.largeprojects.api.event.TransmitNetworkEvent;
import cn.bzgzs.largeprojects.network.NetworkManager;
import cn.bzgzs.largeprojects.network.server.TransmitInitialSpeedPacket;
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
	public static void sendInitialSpeedToPlayer(PlayerEvent.PlayerLoggedInEvent event) {
		if (event.getEntity() instanceof ServerPlayer player) {
			TransmitNetwork network = TransmitNetwork.Factory.get(player.level);
			NetworkManager.INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), new TransmitInitialSpeedPacket(network.getSpeedCollection()));
		}
	}

	@SubscribeEvent
	public static void syncSpeedToClient(TransmitNetworkEvent.UpdateSpeedEvent event) {
		if (event.getLevel() instanceof ServerLevel level) {
			for (ServerPlayer player : level.getPlayers(player -> true)) {
				NetworkManager.INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), new TransmitSpeedSyncPacket(event.getUpdatedData()));
			}
		}
	}
}
