package net.industrybase.api.pipe;

import com.google.common.collect.HashMultimap;
import net.industrybase.api.IndustryBaseApi;
import net.industrybase.api.pipe.unit.*;
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
	private final HashMap<BlockPos, PipeUnit> components = new HashMap<>();
	private final HashMultimap<BlockPos, Direction> connections = HashMultimap.create();
	private final ArrayDeque<Runnable> tasks = new ArrayDeque<>();
	private ArrayDeque<PipeUnit> fluidTasks = new ArrayDeque<>();
	private ArrayDeque<PipeUnit> nextFluidTasks = new ArrayDeque<>();
	private final LevelAccessor level;

	public PipeNetwork(LevelAccessor level) {
		this.level = level;
	}

	public void setPressure(BlockPos pos, Direction direction, double pressure) {
		PipeUnit unit = this.components.get(pos);
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
			PipeUnit primaryUnit = this.components.get(primary);
			PipeUnit secondaryUnit = this.components.get(secondary);

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

						PipeUnit secondaryNeighbor = secondaryUnit.getNeighbor(direction.getOpposite());
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
						PipeUnit[] secondaryCut = ((StraightPipe) secondaryUnit).toRouter(primary);
						for (PipeUnit unit : secondaryCut) {
							unit.forEach(pos -> this.components.put(pos, unit));
						}
					}
					PipeUnit newSecondaryUnit = this.components.get(secondary);
					PipeUnit newPrimaryUnit = StraightPipe.newInstance(primary, connectAxis);
					newPrimaryUnit.setNeighbor(direction.getOpposite(), newSecondaryUnit);
					newSecondaryUnit.setNeighbor(direction, newPrimaryUnit);
					this.components.put(primary, newPrimaryUnit);
				}
			} else if (secondaryUnit == null) {
				if (primaryUnit.canMergeWith(direction.getOpposite())) {
					if (primaryUnit.isSingle()) {
						StraightPipe unit = StraightPipe.newInstance(primary, connectAxis);
						unit.addPipe(secondary);

						PipeUnit primaryNeighbor = primaryUnit.getNeighbor(direction);
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
						PipeUnit[] primaryCut = ((StraightPipe) primaryUnit).toRouter(primary);
						for (PipeUnit unit : primaryCut) {
							unit.forEach(pos -> this.components.put(pos, unit));
						}
					}
					PipeUnit newPrimaryUnit = this.components.get(primary);
					PipeUnit newSecondaryUnit = StraightPipe.newInstance(secondary, connectAxis);
					newPrimaryUnit.setNeighbor(direction.getOpposite(), newSecondaryUnit);
					newSecondaryUnit.setNeighbor(direction, newPrimaryUnit);
					this.components.put(secondary, newSecondaryUnit);
				}
			} else if (primaryUnit != secondaryUnit) {
				boolean primaryCanMerge = primaryUnit.canMergeWith(direction.getOpposite());
				boolean secondaryCanMerge = secondaryUnit.canMergeWith(direction);
				if (primaryCanMerge && secondaryCanMerge) {
					if (!primaryUnit.isSingle()) {
						PipeUnit unit = ((StraightPipe) primaryUnit).merge(direction.getOpposite(), secondaryUnit);
						unit.forEach(pos -> this.components.put(pos, primaryUnit));

						PipeUnit secondaryNeighbor = secondaryUnit.getNeighbor(direction.getOpposite());
						if (secondaryNeighbor != null) secondaryNeighbor.setNeighbor(direction, primaryUnit);
						primaryUnit.setNeighbor(direction.getOpposite(), secondaryNeighbor);
					} else if (!secondaryUnit.isSingle()) {
						PipeUnit unit = ((StraightPipe) secondaryUnit).merge(direction, primaryUnit);
						unit.forEach(pos -> this.components.put(pos, secondaryUnit));

						PipeUnit primaryNeighbor = primaryUnit.getNeighbor(direction);
						if (primaryNeighbor != null) primaryNeighbor.setNeighbor(direction.getOpposite(), secondaryUnit);
						secondaryUnit.setNeighbor(direction, primaryNeighbor);
					} else {
						StraightPipe unit = StraightPipe.newInstance(secondary, connectAxis);
						unit.addPipe(secondary);

						PipeUnit primaryNeighbor = primaryUnit.getNeighbor(direction);
						if (primaryNeighbor != null) primaryNeighbor.setNeighbor(direction.getOpposite(), unit);
						unit.setNeighbor(direction, primaryNeighbor);

						PipeUnit secondaryNeighbor = secondaryUnit.getNeighbor(direction.getOpposite());
						if (secondaryNeighbor != null) secondaryNeighbor.setNeighbor(direction, unit);
						unit.setNeighbor(direction.getOpposite(), secondaryNeighbor);

						this.components.put(secondary, unit);
						this.components.put(primary, unit);
					}
				} else {
					if (connectAxis == primaryUnit.getAxis()) {
						if (secondaryUnit.getType() == UnitType.STRAIGHT_PIPE) {
							PipeUnit[] secondaryCut = ((StraightPipe) secondaryUnit).toRouter(secondary);
							for (PipeUnit unit : secondaryCut) {
								unit.forEach((pos) -> this.components.put(pos, unit));
							}
						}
					} else if (connectAxis == secondaryUnit.getAxis()) {
						if (primaryUnit.getType() == UnitType.STRAIGHT_PIPE) {
							PipeUnit[] primaryCut = ((StraightPipe) primaryUnit).toRouter(primary);
							for (PipeUnit unit : primaryCut) {
								unit.forEach((pos) -> this.components.put(pos, unit));
							}
						}
					} else {
						if (primaryUnit.getType() == UnitType.STRAIGHT_PIPE) {
							PipeUnit[] primaryCut = ((StraightPipe) primaryUnit).toRouter(primary);
							for (PipeUnit unit : primaryCut) {
								unit.forEach((pos) -> this.components.put(pos, unit));
							}
						}
						if (secondaryUnit.getType() == UnitType.STRAIGHT_PIPE) {
							PipeUnit[] secondaryCut = ((StraightPipe) secondaryUnit).toRouter(secondary);
							for (PipeUnit unit : secondaryCut) {
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

			PipeUnit primaryUnit = this.components.get(node);
			PipeUnit secondaryUnit = this.components.get(another);

			PipeUnit unit = primaryUnit.spilt(node, direction);
			unit.forEach((pos) -> this.components.put(pos, unit));
			if (primaryUnit.getType() == UnitType.ROUTER) {
				PipeUnit straight = ((PipeRouter) primaryUnit).toStraightPipe();
				straight.forEach((pos) -> this.components.put(pos, straight)); // prevent empty unit
			}
			if (secondaryUnit.getType() == UnitType.ROUTER) {
				PipeUnit straight = ((PipeRouter) secondaryUnit).toStraightPipe();
				straight.forEach((pos) -> this.components.put(pos, straight));
			}
			// TODO: merge
		}
	}

	public static double square(Direction.Axis axis, AABB aabb1, AABB aabb2) {
		double x = Math.min(aabb1.maxX, aabb2.maxX) - Math.max(aabb1.minX, aabb2.minX);
		double y = Math.min(aabb1.maxY, aabb2.maxY) - Math.max(aabb1.minY, aabb2.minY);
		double z = Math.min(aabb1.maxZ, aabb2.maxZ) - Math.max(aabb1.minZ, aabb2.minZ);
		if (x < 0.0D) x = 0.0D;
		if (y < 0.0D) y = 0.0D;
		if (z < 0.0D) z = 0.0D;
		return switch (axis) {
			case X -> y * z;
			case Y -> x * z;
			case Z -> x * y;
		};
	}

	private void tickConnectTasks() {
		for (Runnable runnable = this.tasks.pollFirst(); runnable != null; runnable = this.tasks.poll()) {
			runnable.run();
		}
	}

	private void tickFluidTasks() {
		ArrayDeque<PipeUnit> tasks = this.fluidTasks;
		this.fluidTasks = this.nextFluidTasks;
		this.nextFluidTasks = tasks;

		int size = tasks.size();
		PipeUnit[] units = new PipeUnit[size];
		for (int i = 0; i < size; i++) {
			PipeUnit unit = this.nextFluidTasks.pollFirst();
			if (unit == null) break;
			units[i] = unit;
			unit.unsetTicked();
		}
		for (PipeUnit unit : units) {
			if (!unit.ticked()) {
				unit.tickTasks();
				unit.setTicked();
			}
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
				PipeNetwork network = get(event.getLevel());
				network.tickConnectTasks();
				network.tickFluidTasks();
			}
		}
	}
}
