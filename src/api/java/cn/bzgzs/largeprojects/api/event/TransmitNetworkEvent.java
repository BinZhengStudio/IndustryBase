package cn.bzgzs.largeprojects.api.event;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraftforge.eventbus.api.Event;

import java.util.Set;

public class TransmitNetworkEvent extends Event {
	private final LevelAccessor level;

	public TransmitNetworkEvent(LevelAccessor level) {
		this.level = level;
	}

	public LevelAccessor getLevel() {
		return this.level;
	}

	public static class UpdateEvent extends TransmitNetworkEvent {
		private Set<BlockPos> updatedNetworks;

		public UpdateEvent(LevelAccessor level, Set<BlockPos> updatedNetworks) {
			super(level);
			this.updatedNetworks = updatedNetworks;
		}

		public Set<BlockPos> getUpdatedNetworks() {
			return this.updatedNetworks;
		}
	}

	public static class UpdatePowerEvent extends UpdateEvent {
		public UpdatePowerEvent(LevelAccessor level, Set<BlockPos> updatedNetworks) {
			super(level, updatedNetworks);
		}
	}

	public static class UpdateResistanceEvent extends UpdateEvent {
		public UpdateResistanceEvent(LevelAccessor level, Set<BlockPos> updatedNetworks) {
			super(level, updatedNetworks);
		}
	}

	public static class UpdateSpeedEvent extends UpdateEvent {
		public UpdateSpeedEvent(LevelAccessor level, Set<BlockPos> updatedNetworks) {
			super(level, updatedNetworks);
		}
	}
}
