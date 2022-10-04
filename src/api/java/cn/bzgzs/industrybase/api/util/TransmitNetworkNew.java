package cn.bzgzs.industrybase.api.util;

import cn.bzgzs.industrybase.api.event.TransmitNetworkEvent;
import com.google.common.collect.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.LevelAccessor;
import net.minecraftforge.common.MinecraftForge;

import java.util.*;

public class TransmitNetworkNew {
	private final LevelAccessor level;
	private final Queue<Runnable> tasks;
	private final Map<ComponentContext, SingleNetwork> networkMap;
//	private final SetMultimap<BlockPos, Direction> connections;

	public TransmitNetworkNew(LevelAccessor level) {
		this.level = level;
		this.tasks = new ArrayDeque<>();
		this.networkMap = new HashMap<>();
//		this.connections = Multimaps.newSetMultimap(new HashMap<>(), () -> EnumSet.noneOf(Direction.class));
	}

	public class SingleNetwork {
		private final LinkedHashMultimap<BlockPos, Direction> components;
		private final Multiset<BlockPos> machinePower;
		private final Multiset<BlockPos> machineResistance;
		private int power;
		private int resistance;

		public SingleNetwork(LinkedHashMultimap<BlockPos, Direction> components) {
			this.machinePower = HashMultiset.create();
			this.machineResistance = HashMultiset.create();
			this.components = components;
		}

		public int size() {
			return this.components.size();
		}

		public int blockSize() {
			return this.components.keySet().size();
		}

		public ComponentContext root() {
			BlockPos pos = blockRoot();
			return new ComponentContext(pos, this.components.get(pos).iterator().next());
		}

		public BlockPos blockRoot() {
			return this.components.keySet().iterator().next();
		}

		public void cut(BlockPos node, Direction direction) {
			if (this.components.remove(node, direction)) {
				BlockPos another = node.offset(direction.getNormal());
				this.components.remove(another, direction.getOpposite());
				BFSIterator nodeIterator = new BFSIterator(node, this);
				BFSIterator anotherIterator = new BFSIterator(another, this);

				while (nodeIterator.hasNext()) {
					BlockPos next = nodeIterator.next();
					if (!anotherIterator.searched.containsKey(next)) {
						// 互换 iterator
						BFSIterator iterator = anotherIterator;
						anotherIterator = nodeIterator;
						nodeIterator = iterator;
						continue;
					}
					return; // 如果两个 iterator 存在重复方块（连通域没有断开），则直接退出
				}

				LinkedHashMultimap<BlockPos, Direction> primaryComponent = this.components;
				LinkedHashSet<BlockPos> secondaryComponent;
				SetMultimap<BlockPos, Direction> secondaryConn;
				BlockPos primaryNode = primaryComponent.iterator().next();
				LinkedHashMultimap<BlockPos, Direction> searched = nodeIterator.getSearched();

				if (searched.containsKey(primaryNode)) {
					secondaryComponent = new LinkedHashSet<>(Sets.difference(primaryComponent, searched));
					secondaryConn = HashMultimap.create(Multimaps.filterKeys(nodeIterator.conn, pos -> !searched.contains(pos))); // TODO Hash?
					primaryComponent.retainAll(searched);
				} else {
					secondaryComponent = searched;
					secondaryConn = nodeIterator.conn;
					primaryComponent.removeAll(searched);
				}

				BlockPos secondaryNode = secondaryComponent.iterator().next();
				Multiset<BlockPos> updatedPower = HashMultiset.create();
				Set<BlockPos> deletedPower = new HashSet<>();
				Multiset<BlockPos> updatedResistance = HashMultiset.create();
				Set<BlockPos> deletedResistance = new HashSet<>();
				Map<BlockPos, BlockPos> updatedRoot = new HashMap<>();
				Set<BlockPos> deletedRoot = new HashSet<>();
				if (secondaryComponent.size() <= 1) {
					this.components.remove(secondaryNode);

					int powerDiff = this.machinePower.count(secondaryNode);
					int resistanceDiff = this.machineResistance.count(secondaryNode);
					this.power -= powerDiff;
					this.resistance -= resistanceDiff; // TODO collection
					if (this.power > 0) {
						updatedPower.setCount(primaryNode, this.power);
					} else {
						deletedPower.add(primaryNode);
					}
					if (this.resistance > 0) {
						updatedPower.setCount(primaryNode, this.resistance);
					} else {
						deletedPower.add(primaryNode);
					}

					this.rootCollection.remove(secondaryNode); // TODO root direction
					deletedRoot.add(secondaryNode);
				} else {
					int powerDiff = 0;
					int resistanceDiff = 0;
					SingleNetwork network = new SingleNetwork(secondaryComponent);
					for (BlockPos pos : secondaryComponent) {
						this.components.remove(pos);
						secondaryConn.get(pos).forEach(side -> networkMap.put(new ComponentContext(pos, side), network));

						powerDiff += this.machinePower.count(pos);
						resistanceDiff += this.machineResistance.count(pos);
						// 将原先从主连通域分离的映射移到子连通域
						this.rootCollection.put(pos, secondaryNode);
						updatedRoot.put(pos, secondaryNode);
					}
					this.power -= powerDiff;
					this.resistance -= resistanceDiff;
					if (this.power > 0) {
						updatedPower.setCount(primaryNode, this.power);
					} else {
						deletedPower.add(primaryNode);
					}
					if (this.resistance > 0) {
						updatedPower.setCount(primaryNode, this.resistance);
					} else {
						deletedPower.add(primaryNode);
					}
					updatedPower.setCount(secondaryNode, this.powerCollection.add(secondaryNode, powerDiff) + powerDiff);
					updatedResistance.setCount(secondaryNode, this.resistanceCollection.add(secondaryNode, resistanceDiff) + resistanceDiff);
				}
				if (primaryComponent.size() <= 1) {
					this.components.remove(primaryNode);

					this.powerCollection.setCount(primaryNode, 0);
					deletedPower.add(primaryNode);
					this.resistanceCollection.setCount(primaryNode, 0);
					deletedResistance.add(primaryNode);
					this.rootCollection.remove(primaryNode);
					deletedRoot.add(primaryNode);
				}
				MinecraftForge.EVENT_BUS.post(new TransmitNetworkEvent.UpdatePowerEvent(this.level, updatedPower, deletedPower));
				MinecraftForge.EVENT_BUS.post(new TransmitNetworkEvent.UpdateResistanceEvent(this.level, updatedResistance, deletedResistance));
				MinecraftForge.EVENT_BUS.post(new TransmitNetworkEvent.UpdateRootEvent(this.level, updatedRoot, deletedRoot));

				this.updateSpeed(primaryNode);
				this.updateSpeed(secondaryNode);
			}
		}
	}

	public record ComponentContext(BlockPos pos, Direction direction) {
	}

	public class BFSIterator implements Iterator<BlockPos> {
		private final LinkedHashMultimap<BlockPos, Direction> searched = LinkedHashMultimap.create();
		private final Queue<BlockPos> queue = new ArrayDeque<>();
		private final SingleNetwork network;

		public BFSIterator(BlockPos node, SingleNetwork network) {
			node = node.immutable();
			this.searched.putAll(node, network.components.get(node));
			this.queue.offer(node);
			this.network = network;
		}

		@Override
		public boolean hasNext() {
			return this.queue.size() > 0;
		}

		@Override
		public BlockPos next() {
			BlockPos node = this.queue.remove();
			for (Direction direction : this.network.components.get(node)) {
				BlockPos another = node.offset(direction.getNormal());
				if (this.searched.putAll(another, this.network.components.get(another))) {
					this.queue.offer(another);
				}
			}
			return node;
		}

		public LinkedHashMultimap<BlockPos, Direction> getSearched() {
			return this.searched;
		}

		public boolean contains(ComponentContext context) {
			this.
			return
		}
	}
}
