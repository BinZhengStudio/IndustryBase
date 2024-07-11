package net.industrybase.api.pipe;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Sets;
import net.industrybase.api.electric.EnergyMap;
import net.industrybase.world.level.block.BlockList;
import net.industrybase.world.level.block.PipeBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.ForgeCapabilities;

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;

public class PipeNetwork {
	private final HashMap<BlockPos, LinkedHashSet<BlockPos>> components = new HashMap<>();
	private final HashMap<BlockPos, PipeUnit> units = new HashMap<>();
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
						this.link(pos, side);
					} else if (this.canConnect(pos.relative(side), side.getOpposite())) {
//						this.FEMachines.put(pos.immutable(), side);
						this.spilt(pos, side);
					} else {
//						this.FEMachines.remove(pos.immutable(), side);
						this.spilt(pos, side);
					}
				} else {
//					this.FEMachines.remove(pos.immutable(), side);
					this.spilt(pos, side);
				}
			}
			callback.run();
		});
	}

	@SuppressWarnings("deprecation")
	private boolean canConnect(BlockPos pos, Direction side) {
		if (this.level.isAreaLoaded(pos, 0)) {
			BlockEntity blockEntity = this.level.getBlockEntity(pos);
			return blockEntity != null && blockEntity.getCapability(ForgeCapabilities.FLUID_HANDLER, side).isPresent();
		}
		return false;
	}

	@SuppressWarnings("deprecation")
	private boolean pipeConnected(BlockPos pos, Direction side) {
		if (this.level.isAreaLoaded(pos, 0)) {
			BlockState state = this.level.getBlockState(pos);
			if (state.is(BlockList.IRON_PIPE.get())) { // TODO
				return state.getValue(PipeBlock.PROPERTIES.get(side));
			}
		}
		return false;
	}

	@SuppressWarnings("deprecation")
	public void removePipe(BlockPos pos, Runnable callback) {
		this.tasks.offer(() -> {
//			this.totalEnergy.shrink(this.root(pos), this.machineEnergy.remove(pos));
			for (Direction side : Direction.values()) {
				this.spilt(pos, side);
			}
//			this.FEMachines.removeAll(pos); // 移除相应的 FE 机器
			callback.run();
		});
	}

	private void link(BlockPos node, Direction direction) {
		BlockPos secondary = node.immutable();
		if (this.connections.put(secondary, direction)) {
			BlockPos primary = secondary.relative(direction);
			this.connections.put(primary, direction.getOpposite());
			LinkedHashSet<BlockPos> primaryComponent = this.components.get(primary);
			LinkedHashSet<BlockPos> secondaryComponent = this.components.get(secondary);

//			EnergyMap.Energy primaryEnergy = this.machineEnergy.get(primary);
//			EnergyMap.Energy secondaryEnergy = this.machineEnergy.get(secondary);

			if (primaryComponent == null && secondaryComponent == null) {
				LinkedHashSet<BlockPos> union = new LinkedHashSet<>();
				this.components.put(secondary, union);
				this.components.put(primary, union);
				union.add(secondary);
				union.add(primary);

//				this.totalEnergy.put(secondary, EnergyMap.Energy.union(primaryEnergy, secondaryEnergy));
//				this.totalEnergy.remove(primary);
//				this.mergeFE(secondary, primary);
			} else if (primaryComponent == null) {
				BlockPos secondaryNode = secondaryComponent.iterator().next();
				this.components.put(primary, secondaryComponent);
				secondaryComponent.add(primary);

//				this.totalEnergy.add(secondaryNode, primaryEnergy);
//				this.totalEnergy.remove(primary);
//				this.mergeFE(secondaryNode, primary);
			} else if (secondaryComponent == null) {
				BlockPos primaryNode = primaryComponent.iterator().next();
				this.components.put(secondary, primaryComponent);
				primaryComponent.add(secondary);

//				this.totalEnergy.add(primaryNode, secondaryEnergy);
//				this.totalEnergy.remove(secondary);
//				this.mergeFE(primaryNode, secondary);
			} else if (primaryComponent != secondaryComponent) {
				BlockPos primaryNode = primaryComponent.iterator().next();
				BlockPos secondaryNode = secondaryComponent.iterator().next();
				LinkedHashSet<BlockPos> union = new LinkedHashSet<>(Sets.union(primaryComponent, secondaryComponent));
				union.forEach(pos -> this.components.put(pos, union));

//				this.totalEnergy.add(primaryNode, this.totalEnergy.remove(secondaryNode));
//				this.mergeFE(primaryNode, secondaryNode);
			}
		}
	}

	private void spilt(BlockPos node, Direction direction) {
		if (this.connections.remove(node, direction)) {
			BlockPos another = node.relative(direction);
			this.connections.remove(another, direction.getOpposite());
			BFSIterator nodeIterator = new BFSIterator(node);
			BFSIterator anotherIterator = new BFSIterator(another);

			while (nodeIterator.hasNext()) {
				BlockPos next = nodeIterator.next();
				if (!anotherIterator.getSearched().contains(next)) {
					BFSIterator iterator = anotherIterator;
					anotherIterator = nodeIterator;
					nodeIterator = iterator;
					continue;
				}
				return;
			}

			LinkedHashSet<BlockPos> primaryComponent = this.components.get(node);
			LinkedHashSet<BlockPos> secondaryComponent;
			BlockPos primaryNode = primaryComponent.getFirst();
			LinkedHashSet<BlockPos> searched = nodeIterator.getSearched();

			if (searched.contains(primaryNode)) {
				secondaryComponent = new LinkedHashSet<>(Sets.difference(primaryComponent, searched));
				primaryComponent.retainAll(searched);
			} else {
				secondaryComponent = searched;
				primaryComponent.removeAll(searched);
			}

			BlockPos secondaryNode = secondaryComponent.getFirst();
			if (secondaryComponent.size() <= 1) {
				this.components.remove(secondaryNode);

//				EnergyMap.Energy diff = this.machineEnergy.get(secondaryNode);
//				this.totalEnergy.shrink(primaryNode, diff);
			} else {
				EnergyMap.TempEnergy diff = new EnergyMap.TempEnergy();
				for (BlockPos pos : secondaryComponent) {
					this.components.put(pos, secondaryComponent);
//					diff.add(this.machineEnergy.get(pos));
				}
//				this.totalEnergy.shrink(primaryNode, diff);
//				this.totalEnergy.put(secondaryNode, diff);
			}
			if (primaryComponent.size() <= 1) {
				this.components.remove(primaryNode);
				// 已在 shrink 中完成对 primaryNode 的能量的检查和清除
			}
			// 分配 FE 能量
//			int primarySize = this.size(primaryNode), secondarySize = this.size(secondaryNode);
//			int diff = this.FEEnergy.count(primaryNode) * secondarySize / (primarySize + secondarySize);
//			this.FEEnergy.remove(primaryNode, diff);
//			this.FEEnergy.add(secondaryNode, diff);
		}
	}

	public class BFSIterator implements Iterator<BlockPos> {
		private final LinkedHashSet<BlockPos> searched = new LinkedHashSet<>();
		private final ArrayDeque<BlockPos> queue = new ArrayDeque<>();

		public BFSIterator(BlockPos node) {
			node = node.immutable();
			this.searched.add(node);
			this.queue.offer(node);
		}

		public boolean hasNext() {
			return !this.queue.isEmpty();
		}

		@Override
		public BlockPos next() {
			BlockPos node = this.queue.remove();
			for (Direction direction : PipeNetwork.this.connections.get(node)) {
				BlockPos another = node.relative(direction);
				if (this.searched.add(another)) {
					this.queue.offer(another);
				}
			}
			return node;
		}

		public LinkedHashSet<BlockPos> getSearched() {
			return this.searched;
		}
	}
}
