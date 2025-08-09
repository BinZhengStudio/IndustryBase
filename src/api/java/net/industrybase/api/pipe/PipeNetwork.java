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
	private final HashMap<BlockPos, AABB> aabbCache = new HashMap<>();
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
			unit.setPressure(this.getTask(), direction, pressure);
		}
	}

	public ArrayDeque<PipeUnit> getTask() {
		return this.fluidTasks;
	}

	public void registerHandler(BlockPos pos, AABB aabb, StorageInterface storageInterface, Runnable callback) {
		this.tasks.addLast(() -> {
			// clean up old pipe
			PipeUnit unit = this.components.get(pos);
			if (unit != null) {
				this.connections.removeAll(pos); // clear connections
				unit.forEachNeighbor((direction, neighbor) -> neighbor.setNeighbor(direction.getOpposite(), null));
			}
			this.components.put(pos.immutable(), new FluidStorage(this, pos, aabb, storageInterface));

			for (Direction side : Direction.values()) {
				if (this.canConnect(pos, side)) {
					if (this.pipeConnected(pos.relative(side), side.getOpposite())) {
						this.link(pos, side);
					} else if (this.canConnect(pos.relative(side), side.getOpposite())) {
						this.linkHandlers(pos, side);
					} else {
						this.spilt(pos, side);
					}
				} else {
					this.spilt(pos, side);
				}
			}
			callback.run();
		});
	}

	public void updateHandler(BlockPos pos, Runnable callback) {
		this.tasks.addLast(() -> {
			PipeUnit unit = this.components.get(pos);
			if (unit == null || unit.getType() != UnitType.FLUID_STORAGE) return; // TODO: or force update?

			for (Direction side : Direction.values()) {
				if (this.canConnect(pos, side)) {
					if (this.pipeConnected(pos.relative(side), side.getOpposite())) {
						this.link(pos, side);
					} else if (this.canConnect(pos.relative(side), side.getOpposite())) {
						this.linkHandlers(pos, side);
					} else {
						this.spilt(pos, side);
					}
				} else {
					this.spilt(pos, side);
				}
			}
			callback.run();
		});
	}

	public void registerPipe(BlockPos pos, AABB aabb, Runnable callback) {
		this.aabbCache.put(pos, aabb);
		this.tasks.addLast(() -> {
			for (Direction side : Direction.values()) {
				if (this.pipeConnected(pos, side)) {
					if (this.pipeConnected(pos.relative(side), side.getOpposite())) {
						this.link(pos, side);
					} else if (!this.canConnect(pos.relative(side), side.getOpposite())) {
						this.spilt(pos, side);
					}
				} else {
					this.spilt(pos, side);
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
				this.spilt(pos, side);
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
				AABB primaryAABB = this.aabbCache.remove(primary);
				AABB secondaryAABB = this.aabbCache.remove(secondary);

				StraightPipe unit = StraightPipe.newInstance(secondary, this, connectAxis, secondaryAABB);
				this.components.put(secondary, unit);
				if (primaryAABB.equals(secondaryAABB)) {
					unit.addPipe(primary);
					this.components.put(primary, unit);
				} else {
					StraightPipe unit2 = StraightPipe.newInstance(primary, this, connectAxis, primaryAABB);
					this.components.put(primary, unit2);
				}
			} else if (primaryUnit == null) {
				AABB primaryAABB = this.aabbCache.remove(primary);
				AABB secondaryAABB = secondaryUnit.getAABB();
				MergeCheckResult result = secondaryUnit.canMergeWith(direction, primaryAABB);

				if (result == MergeCheckResult.PASS) {
					if (secondaryUnit.isSingle()) {
						StraightPipe unit = StraightPipe.newInstance(secondary, this, connectAxis, secondaryAABB);
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
				} else if (result == MergeCheckResult.FAIL_AABB) {
					StraightPipe primaryNewUnit = StraightPipe.newInstance(primary, this, connectAxis, primaryAABB);
					this.components.put(primary, primaryNewUnit);

					if (secondaryUnit.isSingle()) {
						StraightPipe secondaryNewUnit = StraightPipe.newInstance(secondary, this, connectAxis, secondaryAABB);
						this.components.put(secondary, secondaryNewUnit);

						// inherit old neighbor
						PipeUnit secondaryNeighbor = secondaryUnit.getNeighbor(direction.getOpposite());
						if (secondaryNeighbor != null) secondaryNeighbor.setNeighbor(direction, secondaryNewUnit);
						secondaryNewUnit.setNeighbor(direction.getOpposite(), secondaryNeighbor);

						// add neighbor each other
						primaryNewUnit.setNeighbor(direction.getOpposite(), secondaryNewUnit);
						secondaryNewUnit.setNeighbor(direction, primaryNewUnit);
					} else {
						primaryNewUnit.setNeighbor(direction.getOpposite(), secondaryUnit);
						secondaryUnit.setNeighbor(direction, primaryNewUnit);
					}
				} else {
					if (secondaryUnit.getType() == UnitType.STRAIGHT_PIPE) {
						PipeUnit[] secondaryCut = ((StraightPipe) secondaryUnit).toRouter(primary);
						for (PipeUnit unit : secondaryCut) {
							unit.forEach(pos -> this.components.put(pos, unit));
						}
					}
					PipeUnit newSecondaryUnit = this.components.get(secondary);
					PipeUnit newPrimaryUnit = StraightPipe.newInstance(primary, this, connectAxis, primaryAABB);
					newPrimaryUnit.setNeighbor(direction.getOpposite(), newSecondaryUnit);
					newSecondaryUnit.setNeighbor(direction, newPrimaryUnit);
					this.components.put(primary, newPrimaryUnit);
				}
			} else if (secondaryUnit == null) {
				AABB primaryAABB = primaryUnit.getAABB();
				AABB secondaryAABB = this.aabbCache.remove(secondary);
				MergeCheckResult result = primaryUnit.canMergeWith(direction.getOpposite(), secondaryAABB);

				if (result == MergeCheckResult.PASS) {
					if (primaryUnit.isSingle()) {
						StraightPipe unit = StraightPipe.newInstance(primary, this, connectAxis, primaryAABB);
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
				} else if (result == MergeCheckResult.FAIL_AABB) {
					StraightPipe secondaryNewUnit = StraightPipe.newInstance(secondary, this, connectAxis, secondaryAABB);
					this.components.put(secondary, secondaryNewUnit);

					if (primaryUnit.isSingle()) {
						StraightPipe primaryNewUnit = StraightPipe.newInstance(primary, this, connectAxis, primaryAABB);
						this.components.put(primary, primaryNewUnit);

						PipeUnit primaryNeighbor = primaryUnit.getNeighbor(direction);
						if (primaryNeighbor != null) primaryNeighbor.setNeighbor(direction.getOpposite(), primaryNewUnit);
						primaryNewUnit.setNeighbor(direction, primaryNeighbor);

						secondaryNewUnit.setNeighbor(direction, primaryNewUnit);
						primaryNewUnit.setNeighbor(direction.getOpposite(), secondaryNewUnit);
					} else {
						secondaryNewUnit.setNeighbor(direction, primaryUnit);
						primaryUnit.setNeighbor(direction.getOpposite(), secondaryNewUnit);
					}
				} else {
					if (primaryUnit.getType() == UnitType.STRAIGHT_PIPE) {
						PipeUnit[] primaryCut = ((StraightPipe) primaryUnit).toRouter(primary);
						for (PipeUnit unit : primaryCut) {
							unit.forEach(pos -> this.components.put(pos, unit));
						}
					}
					PipeUnit newPrimaryUnit = this.components.get(primary);
					PipeUnit newSecondaryUnit = StraightPipe.newInstance(secondary, this, connectAxis, secondaryAABB);
					newPrimaryUnit.setNeighbor(direction.getOpposite(), newSecondaryUnit);
					newSecondaryUnit.setNeighbor(direction, newPrimaryUnit);
					this.components.put(secondary, newSecondaryUnit);
				}
			} else if (primaryUnit != secondaryUnit) {
				AABB primaryAABB = primaryUnit.getAABB();
				AABB secondaryAABB = secondaryUnit.getAABB();

				MergeCheckResult primaryCanMerge = primaryUnit.canMergeWith(direction.getOpposite(), secondaryAABB);
				MergeCheckResult secondaryCanMerge = secondaryUnit.canMergeWith(direction, primaryAABB);
				if (primaryCanMerge == MergeCheckResult.PASS && secondaryCanMerge == MergeCheckResult.PASS) {
					if (!primaryUnit.isSingle()) {
						PipeUnit unit = ((StraightPipe) primaryUnit).merge(direction.getOpposite(), secondaryUnit);
						unit.forEach(pos -> this.components.put(pos, primaryUnit));
					} else if (!secondaryUnit.isSingle()) {
						PipeUnit unit = ((StraightPipe) secondaryUnit).merge(direction, primaryUnit);
						unit.forEach(pos -> this.components.put(pos, secondaryUnit));
					} else { // TODO merge via StraightPipe#merge and PipeRouter#toStraightPipe
						StraightPipe unit = StraightPipe.newInstance(secondary, this, connectAxis, secondaryAABB);
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
				} else if (primaryCanMerge == MergeCheckResult.FAIL_AABB && secondaryCanMerge == MergeCheckResult.FAIL_AABB) {
					primaryUnit.setNeighbor(direction.getOpposite(), secondaryUnit);
					secondaryUnit.setNeighbor(direction, primaryUnit);
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
					PipeUnit newPrimaryUnit = this.components.get(primary);
					PipeUnit newSecondaryUnit = this.components.get(secondary);
					newPrimaryUnit.setNeighbor(direction.getOpposite(), newSecondaryUnit);
					newSecondaryUnit.setNeighbor(direction, newPrimaryUnit);
				}
			}
		}
	}

	private void linkHandlers(BlockPos node, Direction direction) {
		BlockPos secondary = node.immutable();
		BlockPos primary = secondary.relative(direction);
		PipeUnit primaryUnit = this.components.get(primary);
		if (primaryUnit == null || primaryUnit.getType() != UnitType.FLUID_STORAGE) return;

		if (this.connections.put(secondary, direction)) {
			this.connections.put(primary, direction.getOpposite());
			PipeUnit secondaryUnit = this.components.get(secondary);

			if (primaryUnit != secondaryUnit) {
				primaryUnit.setNeighbor(direction.getOpposite(), secondaryUnit);
				secondaryUnit.setNeighbor(direction, primaryUnit);
			}
		}
	}

	private void spilt(BlockPos node, Direction direction) { // TODO: fluid spilt
		if (this.connections.remove(node, direction)) {
			BlockPos another = node.relative(direction);
			this.connections.remove(another, direction.getOpposite());

			PipeUnit primaryUnit = this.components.get(node);
			PipeUnit secondaryUnit = this.components.get(another);

			// spilt
			PipeUnit unit = primaryUnit.spilt(node, direction);
			unit.forEach((pos) -> this.components.put(pos, unit));

			// convert primary to straight
			if (primaryUnit.getType() == UnitType.ROUTER) {
				PipeUnit straight = ((PipeRouter) primaryUnit).toStraightPipe();
				// don't put straight into map directly, in order to prevent empty unit
				straight.forEach((pos) -> this.components.put(pos, straight));

				if (straight.getType() == UnitType.STRAIGHT_PIPE) {
					StraightPipe pipe = (StraightPipe) straight;
					AABB pipeAABB = pipe.getAABB();
					Direction positiveDirection = Direction.fromAxisAndDirection(pipe.getAxis(), Direction.AxisDirection.POSITIVE);
					Direction negativeDirection = Direction.fromAxisAndDirection(pipe.getAxis(), Direction.AxisDirection.NEGATIVE);

					// merge positive
					PipeUnit positiveNeighbor = straight.getNeighbor(positiveDirection);
					if (positiveNeighbor != null) {
						MergeCheckResult result = pipe.canMergeWith(positiveDirection, positiveNeighbor.getAABB());
						MergeCheckResult neighborResult = positiveNeighbor.canMergeWith(negativeDirection, pipeAABB);
						if (result == MergeCheckResult.PASS && neighborResult == MergeCheckResult.PASS) {
							PipeUnit merged = pipe.merge(positiveDirection, positiveNeighbor);
							merged.forEach((pos) -> this.components.put(pos, pipe));
						}
					}

					// merge negative
					PipeUnit negativeNeighbor = straight.getNeighbor(negativeDirection);
					if (negativeNeighbor != null) {
						MergeCheckResult result = pipe.canMergeWith(negativeDirection, negativeNeighbor.getAABB());
						MergeCheckResult neighborResult = negativeNeighbor.canMergeWith(positiveDirection, pipeAABB);
						if (result == MergeCheckResult.PASS && neighborResult == MergeCheckResult.PASS) {
							PipeUnit merged = pipe.merge(negativeDirection, negativeNeighbor);
							merged.forEach((pos) -> this.components.put(pos, pipe));
						}
					}
				}
			}

			// convert secondary to straight pipe
			if (secondaryUnit.getType() == UnitType.ROUTER) {
				PipeUnit straight = ((PipeRouter) secondaryUnit).toStraightPipe();
				straight.forEach((pos) -> this.components.put(pos, straight));

				if (straight.getType() == UnitType.STRAIGHT_PIPE) {
					StraightPipe pipe = (StraightPipe) straight;
					AABB pipeAABB = pipe.getAABB();
					Direction positiveDirection = Direction.fromAxisAndDirection(pipe.getAxis(), Direction.AxisDirection.POSITIVE);
					Direction negativeDirection = Direction.fromAxisAndDirection(pipe.getAxis(), Direction.AxisDirection.NEGATIVE);

					// merge positive
					PipeUnit positiveNeighbor = straight.getNeighbor(positiveDirection);
					if (positiveNeighbor != null) {
						MergeCheckResult result = pipe.canMergeWith(positiveDirection, positiveNeighbor.getAABB());
						MergeCheckResult neighborResult = positiveNeighbor.canMergeWith(negativeDirection, pipeAABB);
						if (result == MergeCheckResult.PASS && neighborResult == MergeCheckResult.PASS) {
							PipeUnit merged = pipe.merge(positiveDirection, positiveNeighbor);
							merged.forEach((pos) -> this.components.put(pos, pipe));
						}
					}

					// merge negative
					PipeUnit negativeNeighbor = straight.getNeighbor(negativeDirection);
					if (negativeNeighbor != null) {
						MergeCheckResult result = pipe.canMergeWith(negativeDirection, negativeNeighbor.getAABB());
						MergeCheckResult neighborResult = negativeNeighbor.canMergeWith(positiveDirection, pipeAABB);
						if (result == MergeCheckResult.PASS && neighborResult == MergeCheckResult.PASS) {
							PipeUnit merged = pipe.merge(negativeDirection, negativeNeighbor);
							merged.forEach((pos) -> this.components.put(pos, pipe));
						}
					}
				}
			}
		}
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

		for (PipeUnit unit = tasks.pollFirst(); unit != null; unit = tasks.pollFirst()) {
			unit.unsetSubmittedTask();
			unit.tickTasks();
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
