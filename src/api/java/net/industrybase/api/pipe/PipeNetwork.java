package net.industrybase.api.pipe;

import com.google.common.collect.HashMultimap;
import net.industrybase.api.IndustryBaseApi;
import net.industrybase.api.pipe.unit.IPipeUnit;
import net.industrybase.api.pipe.unit.PipeRouter;
import net.industrybase.api.pipe.unit.PipeUnit;
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

	public void registerPipe(BlockPos pos, Runnable callback) {
		this.tasks.addLast(() -> {
			for (Direction side : Direction.values()) {
				if (this.pipeConnected(pos, side)) {
					if (this.pipeConnected(pos.relative(side), side.getOpposite())) {
						this.linkPipe(pos, side);
					} else if (this.canConnect(pos.relative(side), side.getOpposite())) {
//						this.FEMachines.put(pos.immutable(), side);
						this.spiltPipe(pos, side);
					} else {
//						this.FEMachines.remove(pos.immutable(), side);
						this.spiltPipe(pos, side);
					}
				} else {
//					this.FEMachines.remove(pos.immutable(), side);
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
			if (state.is(BlockTagList.PIPE)) { // TODO
				return state.getValue(PipeBlock.PROPERTIES.get(side));
			}
		}
		return false;
	}

	public void removePipe(BlockPos pos, Runnable callback) {
		this.tasks.offer(() -> {
//			this.totalEnergy.shrink(this.root(pos), this.machineEnergy.remove(pos));
			for (Direction side : Direction.values()) {
				this.spiltPipe(pos, side);
			}
//			this.FEMachines.removeAll(pos); // 移除相应的 FE 机器
			callback.run();
		});
	}

	private void linkPipe(BlockPos node, Direction direction) {
		BlockPos secondary = node.immutable();
		Direction.Axis axis = direction.getAxis();
		if (this.connections.put(secondary, direction)) {
			BlockPos primary = secondary.relative(direction);
			this.connections.put(primary, direction.getOpposite());
			IPipeUnit primaryUnit = this.components.get(primary);
			IPipeUnit secondaryUnit = this.components.get(secondary);

//			EnergyMap.Energy primaryEnergy = this.machineEnergy.get(primary);
//			EnergyMap.Energy secondaryEnergy = this.machineEnergy.get(secondary);

			if (primaryUnit == null && secondaryUnit == null) {
				PipeUnit unit = PipeUnit.newInstance(secondary, axis);
				unit.addPipe(primary);
				this.components.put(secondary, unit);
				this.components.put(primary, unit);

//				this.totalEnergy.put(secondary, EnergyMap.Energy.union(primaryEnergy, secondaryEnergy));
//				this.totalEnergy.remove(primary);
//				this.mergeFE(secondary, primary);
			} else if (primaryUnit == null || primaryUnit.isSingle()) {
				Direction.Axis secondaryAxis = secondaryUnit.getAxis();
				if (secondaryUnit.isUnit()) {
					if (secondaryAxis == axis) {
						secondaryUnit.addPipe(primary);
						this.components.put(primary, secondaryUnit);
					} else {
						PipeRouter router = new PipeRouter(secondary);
						this.components.put(secondary, router);
						PipeUnit unit = secondaryUnit.cut(secondary);
						if (unit != null) unit.forEach((pos) -> this.components.put(pos, unit));
						Direction positive = Direction.get(Direction.AxisDirection.POSITIVE, secondaryAxis);
						Direction negative = Direction.get(Direction.AxisDirection.NEGATIVE, secondaryAxis);
						IPipeUnit positiveUnit = this.components.get(secondary.relative(positive));
						IPipeUnit negativeUnit = this.components.get(secondary.relative(negative));
						if (positiveUnit != null) {
							positiveUnit.setNeighbor(negative, router);
							router.setNeighbor(positive, positiveUnit);
						}
						if (negativeUnit != null) {
							negativeUnit.setNeighbor(positive, router);
							router.setNeighbor(negative, negativeUnit);
						}
					}
				} else {
					if (primaryUnit == null) {
						primaryUnit = PipeUnit.newInstance(primary, axis);
					}
					primaryUnit.setNeighbor(direction.getOpposite(), secondaryUnit);
					secondaryUnit.setNeighbor(direction, primaryUnit);
					this.components.put(primary, primaryUnit);
				}

//				this.totalEnergy.add(secondaryNode, primaryEnergy);
//				this.totalEnergy.remove(primary);
//				this.mergeFE(secondaryNode, primary);
			} else if (secondaryUnit == null || secondaryUnit.isSingle()) {
				Direction.Axis primaryAxis = primaryUnit.getAxis();
				if (primaryUnit.isUnit()) {
					if (primaryAxis == axis) {
						primaryUnit.addPipe(secondary);
						this.components.put(secondary, primaryUnit);
					} else {
						PipeRouter router = new PipeRouter(primary);
						this.components.put(primary, router);
						PipeUnit unit = primaryUnit.cut(primary);
						if (unit != null) unit.forEach((pos) -> this.components.put(pos, unit));
						Direction positive = Direction.get(Direction.AxisDirection.POSITIVE, primaryAxis);
						Direction negative = Direction.get(Direction.AxisDirection.NEGATIVE, primaryAxis);
						IPipeUnit positiveUnit = this.components.get(primary.relative(positive));
						IPipeUnit negativeUnit = this.components.get(primary.relative(negative));
						if (positiveUnit != null) {
							positiveUnit.setNeighbor(negative, router);
							router.setNeighbor(positive, positiveUnit);
						}
						if (negativeUnit != null) {
							negativeUnit.setNeighbor(positive, router);
							router.setNeighbor(negative, negativeUnit);
						}
					}
				} else {
					if (secondaryUnit == null) {
						secondaryUnit = PipeUnit.newInstance(secondary, axis);
					}
					primaryUnit.setNeighbor(direction.getOpposite(), secondaryUnit);
					secondaryUnit.setNeighbor(direction, primaryUnit);
					this.components.put(secondary, secondaryUnit);
				}

//				this.totalEnergy.add(primaryNode, secondaryEnergy);
//				this.totalEnergy.remove(secondary);
//				this.mergeFE(primaryNode, secondary);
			} else if (primaryUnit != secondaryUnit) {
				if (primaryUnit.isRouter() || secondaryUnit.isRouter()) {
					secondaryUnit.setNeighbor(direction, primaryUnit);
					primaryUnit.setNeighbor(direction.getOpposite(), secondaryUnit);
				} else {
					Direction.Axis primaryAxis = primaryUnit.getAxis();
					Direction.Axis secondaryAxis = secondaryUnit.getAxis();
					if (primaryAxis == secondaryAxis) {
						if (axis == primaryAxis) {
							PipeUnit unit = primaryUnit.link(secondaryUnit);
							if (unit != null) {
								for (BlockPos pos : unit) {
									this.components.put(pos, primaryUnit);
								}
							}
						} else {
							PipeRouter primaryRouter = new PipeRouter(primary);
							PipeRouter secondaryRouter = new PipeRouter(secondary);

							PipeUnit unit1 = primaryUnit.cut(primary);
							if (unit1 != null) {
								unit1.forEach((pos) -> this.components.put(pos, unit1));
								unit1.link(primaryRouter);
							}
							primaryUnit.link(primaryRouter);
							this.components.put(primary, primaryRouter);

							PipeUnit unit2 = secondaryUnit.cut(secondary);
							if (unit2 != null) {
								unit2.forEach((pos) -> this.components.put(pos, unit2));
								unit2.link(secondaryRouter);
							}
							secondaryUnit.link(secondaryRouter);
							this.components.put(secondary, secondaryRouter);

							primaryRouter.setNeighbor(direction.getOpposite(), secondaryRouter);
							secondaryRouter.setNeighbor(direction, primaryRouter);
						}
					}
				}

//				this.totalEnergy.add(primaryNode, this.totalEnergy.remove(secondaryNode));
//				this.mergeFE(primaryNode, secondaryNode);
			}
		}
	}

	private void spiltPipe(BlockPos node, Direction direction) {
		if (this.connections.remove(node, direction)) {
			BlockPos another = node.relative(direction);
			this.connections.remove(another, direction.getOpposite());

			IPipeUnit primaryUnit = this.components.get(node);
			if (primaryUnit != null) {
				PipeUnit unit = primaryUnit.spilt(node, direction);
				if (unit != null) unit.forEach((pos) -> this.components.put(pos, unit));
			}

			// 分配 FE 能量
//			int primarySize = this.size(primaryNode), secondarySize = this.size(secondaryNode);
//			int diff = this.FEEnergy.count(primaryNode) * secondarySize / (primarySize + secondarySize);
//			this.FEEnergy.remove(primaryNode, diff);
//			this.FEEnergy.add(secondaryNode, diff);
		}
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
//				get(event.getLevel()).tickEnd();
			}
		}
	}
}
