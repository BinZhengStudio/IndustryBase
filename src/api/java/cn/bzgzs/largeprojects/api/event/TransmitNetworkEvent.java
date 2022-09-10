package cn.bzgzs.largeprojects.api.event;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraftforge.eventbus.api.Event;

import java.util.HashMap;
import java.util.Map;

public class TransmitNetworkEvent extends Event {
	private final LevelAccessor level;

	public TransmitNetworkEvent(LevelAccessor level) {
		this.level = level;
	}

	public LevelAccessor getLevel() {
		return this.level;
	}

	public static abstract class UpdateEvent<T> extends TransmitNetworkEvent {
		protected final T updatedData;

		public UpdateEvent(LevelAccessor level, T updatedData) {
			super(level);
			this.updatedData = updatedData;
		}

		public abstract T getUpdatedData();
	}

	public static class UpdatePowerEvent extends UpdateEvent<Multiset<BlockPos>> {
		public UpdatePowerEvent(LevelAccessor level, Multiset<BlockPos> updatedNetworks) {
			super(level, updatedNetworks);
		}

		@Override
		public Multiset<BlockPos> getUpdatedData() {
			return HashMultiset.create(this.updatedData);
		}
	}

	public static class UpdateResistanceEvent extends UpdateEvent<Multiset<BlockPos>> {
		public UpdateResistanceEvent(LevelAccessor level, Multiset<BlockPos> updatedNetworks) {
			super(level, updatedNetworks);
		}

		@Override
		public Multiset<BlockPos> getUpdatedData() {
			return HashMultiset.create(this.updatedData);
		}
	}

	public static class UpdateSpeedEvent extends UpdateEvent<Map<BlockPos, Double>> {
		public UpdateSpeedEvent(LevelAccessor level, Map<BlockPos, Double> updatedNetworks) {
			super(level, updatedNetworks);
		}

		@Override
		public Map<BlockPos, Double> getUpdatedData() {
			return new HashMap<>(this.updatedData);
		}
	}

	public static class UpdateRootEvent extends UpdateEvent<Map<BlockPos,BlockPos>> {
		public UpdateRootEvent(LevelAccessor level, Map<BlockPos,BlockPos> updatedData) {
			super(level, updatedData);
		}

		@Override
		public Map<BlockPos,BlockPos> getUpdatedData() {
			return new HashMap<>(this.updatedData);
		}
	}
}
