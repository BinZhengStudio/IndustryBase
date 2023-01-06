package cn.bzgzs.industrybase.api.event;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraftforge.eventbus.api.Event;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class TransmitNetworkEvent extends Event { // TODO 使用 ImmutableSet
	private final LevelAccessor level;

	public TransmitNetworkEvent(LevelAccessor level) {
		this.level = level;
	}

	public LevelAccessor getLevel() {
		return this.level;
	}

	public static abstract class UpdateEvent<T, S> extends TransmitNetworkEvent {
		protected final T changed;
		protected final S deleted;

		public UpdateEvent(LevelAccessor level, T changed, S deleted) {
			super(level);
			this.changed = changed;
			this.deleted = deleted;
		}

		public abstract T getChanged();

		public abstract S getDeleted();
	}

	public static class UpdatePowerEvent extends UpdateEvent<Multiset<BlockPos>, Set<BlockPos>> {

		public UpdatePowerEvent(LevelAccessor level, Multiset<BlockPos> changed, Set<BlockPos> deleted) {
			super(level, changed, deleted);
		}

		@Override
		public Multiset<BlockPos> getChanged() {
			return HashMultiset.create(this.changed);
		}

		@Override
		public Set<BlockPos> getDeleted() {
			return new HashSet<>(this.deleted);
		}
	}

	public static class UpdateResistanceEvent extends UpdateEvent<Multiset<BlockPos>, Set<BlockPos>> {
		public UpdateResistanceEvent(LevelAccessor level, Multiset<BlockPos> changed, Set<BlockPos> deleted) {
			super(level, changed, deleted);
		}

		@Override
		public Multiset<BlockPos> getChanged() {
			return HashMultiset.create(this.changed);
		}

		@Override
		public Set<BlockPos> getDeleted() {
			return new HashSet<>(this.deleted);
		}
	}

	public static class UpdateSpeedEvent extends UpdateEvent<Map<BlockPos, Double>, Set<BlockPos>> {
		public UpdateSpeedEvent(LevelAccessor level, Map<BlockPos, Double> changed, Set<BlockPos> deleted) {
			super(level, changed, deleted);
		}

		@Override
		public Map<BlockPos, Double> getChanged() {
			return new HashMap<>(this.changed);
		}

		@Override
		public Set<BlockPos> getDeleted() {
			return new HashSet<>(this.deleted);
		}
	}

	public static class UpdateRootEvent extends UpdateEvent<Map<BlockPos,BlockPos>, Set<BlockPos>> {
		public UpdateRootEvent(LevelAccessor level, Map<BlockPos, BlockPos> changed, Set<BlockPos> deleted) {
			super(level, changed, deleted);
		}

		@Override
		public Map<BlockPos,BlockPos> getChanged() {
			return new HashMap<>(this.changed);
		}

		@Override
		public Set<BlockPos> getDeleted() {
			return new HashSet<>(this.deleted);
		}
	}
}
