package net.industrybase.api.transmit;

import com.google.common.collect.HashMultimap;
import net.industrybase.api.network.ApiNetworkManager;
import net.industrybase.api.transmit.network.server.DomainSpiltPacket;
import net.industrybase.api.transmit.network.server.RootSyncPacket;
import net.industrybase.api.transmit.network.server.SpeedSyncPacket;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.LevelAccessor;

public class TransmitServerNetwork extends TransmitNetwork {
	private final HashMultimap<BlockPos, ServerPlayer> subscribers; // BlockPos is the root block

	public TransmitServerNetwork(LevelAccessor level) {
		super(level);
		if (level.isClientSide()) throw new IllegalArgumentException("client level can't be used in server transmit network");
		this.subscribers = HashMultimap.create();
	}

	public float subscribeSpeed(BlockPos pos, ServerPlayer player) {
		BlockPos root = this.root(pos);
		this.subscribers.put(root, player);
		return this.speeds.getOrDefault(root, 0.0F);
	}

	public void unsubscribe(BlockPos pos, ServerPlayer player) {
		this.subscribers.remove(pos, player); // TODO waiting for code
	}

	protected void updateSpeed(BlockPos root) {
		float speed = 0.0F;
		if (this.components.containsKey(root)) {
			int power = this.totalPower.count(root);
			int resistance = this.totalResistance.count(root);
			if (power > 0 && resistance > 0) {
				speed = (float) power / resistance;
			} else if (power > 0) {
				speed = Float.MAX_VALUE;
			}
		}
		if (speed > 0.0F) {
			this.speeds.put(root, speed);
		} else {
			this.speeds.remove(root);
		}
		ApiNetworkManager.sendToPlayers(new SpeedSyncPacket(root, speed), this.subscribers.get(root));
	}

	@Override
	protected void afterSplit(BlockPos primaryRoot, BlockPos secondaryRoot) {
		this.updateSpeed(primaryRoot);
		this.updateSpeed(secondaryRoot);
		ApiNetworkManager.sendToPlayers(new DomainSpiltPacket(primaryRoot), this.subscribers.get(primaryRoot));
	}

	@Override
	protected void afterMerge(BlockPos primaryRoot, BlockPos secondaryRoot) {
		this.updateSpeed(primaryRoot);
		ApiNetworkManager.sendToPlayers(new RootSyncPacket(secondaryRoot, primaryRoot), this.subscribers.get(secondaryRoot));
	}
}
