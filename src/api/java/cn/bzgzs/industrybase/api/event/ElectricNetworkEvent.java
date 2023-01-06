package cn.bzgzs.industrybase.api.event;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraftforge.eventbus.api.Event;

import java.util.Collection;
import java.util.Map;

public class ElectricNetworkEvent extends Event { // TODO 使用 ImmutableSet
	private final LevelAccessor level;

	public ElectricNetworkEvent(LevelAccessor level) {
		this.level = level;
	}

	public LevelAccessor getLevel() {
		return this.level;
	}

	public static abstract class UpdateEvent<T> extends ElectricNetworkEvent {
		protected final T changed;

		public UpdateEvent(LevelAccessor level, T changed) {
			super(level);
			this.changed = changed;
		}

		public T getChanged() {
			return this.changed;
		}
	}

	public static class AddWireEvent extends UpdateEvent<Map<BlockPos, Collection<BlockPos>>> {
		public AddWireEvent(LevelAccessor level, Map<BlockPos, Collection<BlockPos>> changed) {
			super(level, changed);
		}
	}

	public static class RemoveWireEvent extends UpdateEvent<Map<BlockPos, Collection<BlockPos>>> {
		public RemoveWireEvent(LevelAccessor level, Map<BlockPos, Collection<BlockPos>> changed) {
			super(level, changed);
		}
	}
}
