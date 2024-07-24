package net.industrybase.api.pipe;

import com.google.common.collect.HashMultimap;
import net.industrybase.api.tags.BlockTagList;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.Capabilities;

import java.util.ArrayDeque;
import java.util.HashMap;

public class PipeNetwork {
	private final HashMap<BlockPos, IPipeUnit> components = new HashMap<>();
	private final HashMap<BlockPos, PipeUnit> units = new HashMap<>();
	private final HashMap<BlockPos, PipeRouter> routers = new HashMap<>();
	private final HashMultimap<BlockPos, Direction> connections = HashMultimap.create();
	private final ArrayDeque<Runnable> tasks = new ArrayDeque<>();
	private final LevelAccessor level;

	public PipeNetwork(LevelAccessor level) {
		this.level = level;
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
				PipeUnit unit = new PipeUnit(secondary, axis);
				unit.addPipe(primary);
				this.components.put(secondary, unit);
				this.components.put(primary, unit);

//				this.totalEnergy.put(secondary, EnergyMap.Energy.union(primaryEnergy, secondaryEnergy));
//				this.totalEnergy.remove(primary);
//				this.mergeFE(secondary, primary);
			} else if (primaryUnit == null || primaryUnit.isSingle()) {
				Direction.Axis secondaryAxis = secondaryUnit.getAxis();
				if (!secondaryUnit.isRouter()) {
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
						primaryUnit = new PipeUnit(primary, axis);
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
				if (!primaryUnit.isRouter()) {
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
						secondaryUnit = new PipeUnit(secondary, axis);
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
}
