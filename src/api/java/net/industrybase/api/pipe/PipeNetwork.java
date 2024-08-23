package net.industrybase.api.pipe;

import com.google.common.collect.HashMultimap;
import net.industrybase.api.IndustryBaseApi;
import net.industrybase.api.pipe.unit.*;
import net.industrybase.api.tags.BlockTagList;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.event.level.LevelEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;

import java.util.*;

public class PipeNetwork {
	private final HashMap<BlockPos, IPipeUnit> components = new HashMap<>();
	private final HashMultimap<BlockPos, Direction> connections = HashMultimap.create();
	private final ArrayDeque<Runnable> tasks = new ArrayDeque<>();
	private ArrayDeque<Runnable> fluidTasks = new ArrayDeque<>();
	private ArrayDeque<Runnable> nextFluidTasks = new ArrayDeque<>();
	private final LevelAccessor level;

	public PipeNetwork(LevelAccessor level) {
		this.level = level;
	}

	public void setPressure(BlockPos pos, Direction direction, double pressure) {
		IPipeUnit unit = this.components.get(pos);
		if (unit != null) {
			unit.setPressure(this.fluidTasks, this.nextFluidTasks, direction, pressure);
		}
	}

	public void registerHandler(BlockPos pos, StorageInterface storageInterface, Runnable callback) {
		this.tasks.addLast(() -> {
			FluidStorage storage = new FluidStorage(pos, storageInterface);
			this.components.put(pos.immutable(), storage);

			for (Direction side : Direction.values()) {
				if (this.canConnect(pos, side)) {
					if (this.pipeConnected(pos.relative(side), side.getOpposite())) {
						this.link(pos, side);
					} else if (this.canConnect(pos.relative(side), side.getOpposite())){
						this.link(pos, side);
					} else {
						this.spiltPipe(pos, side);
					}
				} else {
					this.spiltPipe(pos, side);
				}
			}
			callback.run();
		});
	}

	public void registerPipe(BlockPos pos, Runnable callback) {
		this.tasks.addLast(() -> {
			for (Direction side : Direction.values()) {
				if (this.pipeConnected(pos, side)) {
					if (this.pipeConnected(pos.relative(side), side.getOpposite())) {
						this.link(pos, side);
					} else if (!this.canConnect(pos.relative(side), side.getOpposite())){
						this.spiltPipe(pos, side);
					}
				} else {
					this.spiltPipe(pos, side);
				}
			}
			callback.run();
		});
	}

	private boolean canConnect(BlockPos pos, Direction side) {
		if (this.level.isAreaLoaded(pos, 0)) {
			BlockEntity blockEntity = this.level.getBlockEntity(pos);
			if (blockEntity != null) {
				Level level = blockEntity.getLevel();
				if (level != null) {
					return level.getCapability(Capabilities.FluidHandler.BLOCK, pos, null, blockEntity, side) != null;
				}
			}
		}
		return false;
	}

	private boolean pipeConnected(BlockPos pos, Direction side) {
		if (this.level.isAreaLoaded(pos, 0)) {
			BlockState state = this.level.getBlockState(pos);
			try {
				return state.getValue(PipeBlock.PROPERTIES.get(side)); // TODO: different blocks
			} catch (Exception e) {
				return false;
			}
		}
		return false;
	}

	public void removePipe(BlockPos pos, Runnable callback) {
		this.tasks.offer(() -> {
			for (Direction side : Direction.values()) {
				this.spiltPipe(pos, side);
			}
			this.components.remove(pos);
			callback.run();
		});
	}

	private void link(BlockPos node, Direction direction) { // TODO: fluid merge
		BlockPos secondary = node.immutable();
		Direction.Axis connectAxis = direction.getAxis();
		if (this.connections.put(secondary, direction)) {
			BlockPos primary = secondary.relative(direction);
			this.connections.put(primary, direction.getOpposite());
			IPipeUnit primaryUnit = this.components.get(primary);
			IPipeUnit secondaryUnit = this.components.get(secondary);

			if (primaryUnit == null && secondaryUnit == null) {
				StraightPipe unit = StraightPipe.newInstance(secondary, connectAxis);
				unit.addPipe(primary);
				this.components.put(secondary, unit);
				this.components.put(primary, unit);
			} else if (primaryUnit == null) {
				if (secondaryUnit.canMergeWith(direction)) {
					if (secondaryUnit.isSingle()) {
						StraightPipe unit = StraightPipe.newInstance(secondary, connectAxis);
						unit.addPipe(primary);

						IPipeUnit secondaryNeighbor = secondaryUnit.getNeighbor(direction.getOpposite());
						if (secondaryNeighbor != null) secondaryNeighbor.setNeighbor(direction, unit);
						unit.setNeighbor(direction.getOpposite(), secondaryNeighbor);

						this.components.put(secondary, unit);
						this.components.put(primary, unit);
					} else {
						secondaryUnit.addPipe(primary);
						this.components.put(primary, secondaryUnit);
					}
				} else {
					if (secondaryUnit.getType() == UnitType.STRAIGHT_PIPE) {
						IPipeUnit[] primaryCut = ((StraightPipe) secondaryUnit).toRouter(primary);
						for (IPipeUnit unit : primaryCut) {
							unit.forEach(pos -> this.components.put(pos, unit));
						}
					}
					StraightPipe unit = StraightPipe.newInstance(primary, connectAxis);
					unit.setNeighbor(direction.getOpposite(), secondaryUnit);
					this.components.get(secondary).setNeighbor(direction, unit);
					this.components.put(primary, unit);
				}
			} else if (secondaryUnit == null) {
				if (primaryUnit.canMergeWith(direction.getOpposite())) {
					if (primaryUnit.isSingle()) {
						StraightPipe unit = StraightPipe.newInstance(secondary, connectAxis);
						unit.addPipe(secondary);

						IPipeUnit primaryNeighbor = primaryUnit.getNeighbor(direction);
						if (primaryNeighbor != null) primaryNeighbor.setNeighbor(direction.getOpposite(), unit);
						unit.setNeighbor(direction, primaryNeighbor);

						this.components.put(secondary, unit);
						this.components.put(primary, unit);
					} else {
						primaryUnit.addPipe(secondary);
						this.components.put(secondary, primaryUnit);
					}
				} else {
					if (primaryUnit.getType() == UnitType.STRAIGHT_PIPE) {
						IPipeUnit[] primaryCut = ((StraightPipe) primaryUnit).toRouter(primary);
						for (IPipeUnit unit : primaryCut) {
							unit.forEach(pos -> this.components.put(pos, unit));
						}
					}
					IPipeUnit unit = StraightPipe.newInstance(primary, connectAxis);
					this.components.get(primary).setNeighbor(direction.getOpposite(), unit);
					unit.setNeighbor(direction, primaryUnit);
					this.components.put(secondary, unit);
				}
			} else if (primaryUnit != secondaryUnit) {
				boolean primaryCanMerge = primaryUnit.canMergeWith(direction.getOpposite());
				boolean secondaryCanMerge = secondaryUnit.canMergeWith(direction);
				if (primaryCanMerge && secondaryCanMerge) {
					if (!primaryUnit.isSingle()) {
						IPipeUnit unit = ((StraightPipe) primaryUnit).merge(direction.getOpposite(), secondaryUnit);
						unit.forEach(pos -> this.components.put(pos, primaryUnit));

						IPipeUnit secondaryNeighbor = secondaryUnit.getNeighbor(direction.getOpposite());
						if (secondaryNeighbor != null) secondaryNeighbor.setNeighbor(direction, primaryUnit);
						primaryUnit.setNeighbor(direction.getOpposite(), secondaryNeighbor);
					} else if (!secondaryUnit.isSingle()) {
						IPipeUnit unit = ((StraightPipe) secondaryUnit).merge(direction, primaryUnit);
						unit.forEach(pos -> this.components.put(pos, secondaryUnit));

						IPipeUnit primaryNeighbor = primaryUnit.getNeighbor(direction);
						if (primaryNeighbor != null) primaryNeighbor.setNeighbor(direction.getOpposite(), secondaryUnit);
						secondaryUnit.setNeighbor(direction, primaryNeighbor);
					} else {
						StraightPipe unit = StraightPipe.newInstance(secondary, connectAxis);
						unit.addPipe(secondary);

						IPipeUnit primaryNeighbor = primaryUnit.getNeighbor(direction);
						if (primaryNeighbor != null) primaryNeighbor.setNeighbor(direction.getOpposite(), unit);
						unit.setNeighbor(direction, primaryNeighbor);

						IPipeUnit secondaryNeighbor = secondaryUnit.getNeighbor(direction.getOpposite());
						if (secondaryNeighbor != null) secondaryNeighbor.setNeighbor(direction, unit);
						unit.setNeighbor(direction.getOpposite(), secondaryNeighbor);

						this.components.put(secondary, unit);
						this.components.put(primary, unit);
					}
				} else {
					if (connectAxis == primaryUnit.getAxis()) {
						if (secondaryUnit.getType() == UnitType.STRAIGHT_PIPE) {
							IPipeUnit[] secondaryCut = ((StraightPipe) secondaryUnit).toRouter(secondary);
							for (IPipeUnit unit : secondaryCut) {
								unit.forEach((pos) -> this.components.put(pos, unit));
							}
						}
					} else if (connectAxis == secondaryUnit.getAxis()) {
						if (primaryUnit.getType() == UnitType.STRAIGHT_PIPE) {
							IPipeUnit[] primaryCut = ((StraightPipe) primaryUnit).toRouter(primary);
							for (IPipeUnit unit : primaryCut) {
								unit.forEach((pos) -> this.components.put(pos, unit));
							}
						}
					} else {
						if (primaryUnit.getType() == UnitType.STRAIGHT_PIPE) {
							IPipeUnit[] primaryCut = ((StraightPipe) primaryUnit).toRouter(primary);
							for (IPipeUnit unit : primaryCut) {
								unit.forEach((pos) -> this.components.put(pos, unit));
							}
						}
						if (secondaryUnit.getType() == UnitType.STRAIGHT_PIPE) {
							IPipeUnit[] secondaryCut = ((StraightPipe) secondaryUnit).toRouter(secondary);
							for (IPipeUnit unit : secondaryCut) {
								unit.forEach((pos) -> this.components.put(pos, unit));
							}
						}
					}
					// re get the unit because of unit update
					this.components.get(primary).setNeighbor(direction.getOpposite(), secondaryUnit);
					this.components.get(secondary).setNeighbor(direction, primaryUnit);
				}
			}
		}
	}

	private void spiltPipe(BlockPos node, Direction direction) { // TODO: fluid spilt
		if (this.connections.remove(node, direction)) {
			BlockPos another = node.relative(direction);
			this.connections.remove(another, direction.getOpposite());

			IPipeUnit primaryUnit = this.components.get(node);
			IPipeUnit secondaryUnit = this.components.get(another);

			IPipeUnit unit = primaryUnit.spilt(node, direction);
			unit.forEach((pos) -> this.components.put(pos, unit));
			if (primaryUnit.getType() == UnitType.ROUTER) {
				IPipeUnit straight = ((PipeRouter) primaryUnit).toStraightPipe();
				straight.forEach((pos) -> this.components.put(pos, straight)); // prevent empty unit
			}
			if (secondaryUnit.getType() == UnitType.ROUTER) {
				IPipeUnit straight = ((PipeRouter) secondaryUnit).toStraightPipe();
				straight.forEach((pos) -> this.components.put(pos, straight));
			}
			// TODO: merge
		}
	}

	public static double square(Direction.Axis axis, AABB aabb1, AABB aabb2) {
		double x = Math.max(aabb1.minX, aabb2.minX) - Math.min(aabb1.maxX, aabb2.maxX);
		double y = Math.max(aabb1.minY, aabb2.minY) - Math.min(aabb1.maxY, aabb2.maxY);
		double z = Math.max(aabb1.minZ, aabb2.minZ) - Math.min(aabb1.maxZ, aabb2.maxZ);
		if (x < 0.0D) x = 0.0D;
		if (y < 0.0D) y = 0.0D;
		if (z < 0.0D) z = 0.0D;
		return switch (axis) {
			case X -> y * z;
			case Y -> x * z;
			case Z -> x * y;
		};
	}

	private void serverTickStart() {
		for (Runnable runnable = this.tasks.pollFirst(); runnable != null; runnable = this.tasks.poll()) {
			runnable.run();
		}
	}

	private void serverTickEnd() {
		ArrayDeque<Runnable> tasks = this.fluidTasks;
		this.fluidTasks = this.nextFluidTasks;
		this.nextFluidTasks = tasks;
		for (Runnable runnable = this.nextFluidTasks.pollFirst(); runnable != null; runnable = this.nextFluidTasks.pollFirst()) {
			runnable.run();
		}
	}

	@EventBusSubscriber(modid = IndustryBaseApi.MODID)
	public static class Manager {
		private static final Map<LevelAccessor, PipeNetwork> INSTANCES = new IdentityHashMap<>();

		public static PipeNetwork get(LevelAccessor level) {
			return INSTANCES.computeIfAbsent(Objects.requireNonNull(level, "Level can't be null!"), PipeNetwork::new);
		}

		@SubscribeEvent
		public static void onUnload(LevelEvent.Unload event) {
			INSTANCES.remove(event.getLevel());
		}

		@SubscribeEvent
		public static void onLevelTick(LevelTickEvent.Pre event) {
			if (!event.getLevel().isClientSide) {
				get(event.getLevel()).serverTickStart();
			}
		}

		@SubscribeEvent
		public static void onLevelTick(LevelTickEvent.Post event) {
			if (!event.getLevel().isClientSide) {
				get(event.getLevel()).serverTickEnd();
			}
		}
	}
}
