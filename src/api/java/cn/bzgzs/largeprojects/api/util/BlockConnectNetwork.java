package cn.bzgzs.largeprojects.api.util;

import com.google.common.collect.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

import java.util.*;

public class BlockConnectNetwork implements IBlockNetwork {
	private final Map<BlockPos, Set<BlockPos>> components;
	private final SetMultimap<BlockPos, Direction> connections;

	public BlockConnectNetwork() {
		this.components = new HashMap<>();
		this.connections = Multimaps.newSetMultimap(Maps.newHashMap(), () -> EnumSet.noneOf(Direction.class));
	}

	public SetMultimap<BlockPos, Direction> getConnections() {
		return this.connections;
	}

	@Override
	public int size(BlockPos node) {
		return this.components.containsKey(node) ? this.components.get(node).size() : 1;
	}

	@Override
	public BlockPos root(BlockPos node) {
		return this.components.containsKey(node) ? this.components.get(node).iterator().next() : node;
	}

	@Override
	public void cut(BlockPos nodePos, Direction direction, ConnectivityListener afterSplit) {
		if (this.connections.remove(nodePos, direction)) {
			BlockPos another = nodePos.offset(direction.getNormal());
			this.connections.remove(another, direction.getOpposite());
			BFSIterator nodeIterator = new BFSIterator(nodePos);
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

			Set<BlockPos> primaryComponent = this.components.get(nodePos), secondaryComponent;
			BlockPos primaryNode = primaryComponent.iterator().next();
			Set<BlockPos> searched = nodeIterator.getSearched();

			if (searched.contains(primaryNode)) {
				secondaryComponent = Sets.newLinkedHashSet(Sets.difference(primaryComponent, searched));
				primaryComponent.retainAll(searched);
			} else {
				secondaryComponent = searched;
				primaryComponent.removeAll(searched);
			}
			if (secondaryComponent.size() <= 1) {
				secondaryComponent.forEach(this.components::remove);
			} else {
				secondaryComponent.forEach(pos -> this.components.put(pos, secondaryComponent));
			}
			if (primaryComponent.size() <= 1) {
				primaryComponent.forEach(this.components::remove);
			}
			afterSplit.onChange(primaryNode, secondaryComponent.iterator().next());
		}
	}

	@Override
	public void link(BlockPos nodePos, Direction direction, ConnectivityListener beforeMerge) {
		if (this.connections.put(nodePos, direction)) {
			BlockPos connectPos = nodePos.offset(direction.getNormal());
			this.connections.put(connectPos, direction.getOpposite());
			Set<BlockPos> primaryComponent = this.components.get(connectPos);
			Set<BlockPos> secondaryComponent = this.components.get(nodePos);

			if (primaryComponent == null && secondaryComponent == null) {
				Set<BlockPos> union = Sets.newLinkedHashSet();
				beforeMerge.onChange(nodePos, connectPos);
				this.components.put(nodePos, union);
				this.components.put(connectPos, union);
				union.add(nodePos);
				union.add(connectPos);
			} else if (primaryComponent == null) {
				beforeMerge.onChange(secondaryComponent.iterator().next(), connectPos);
				this.components.put(connectPos, secondaryComponent);
				secondaryComponent.add(connectPos);
			} else if (secondaryComponent == null) {
				beforeMerge.onChange(primaryComponent.iterator().next(), nodePos);
				this.components.put(nodePos, primaryComponent);
				primaryComponent.add(nodePos);
			} else if (primaryComponent != secondaryComponent) {
				beforeMerge.onChange(primaryComponent.iterator().next(), secondaryComponent.iterator().next());
				Set<BlockPos> union = Sets.newLinkedHashSet(Sets.union(primaryComponent, secondaryComponent));
				union.forEach(pos -> this.components.put(pos, union));
			}
		}
	}

	public class BFSIterator implements Iterator<BlockPos> {
		private final Set<BlockPos> searched = Sets.newLinkedHashSet();
		private final Queue<BlockPos> queue = Queues.newArrayDeque();

		public BFSIterator(BlockPos node) {
			this.searched.add(node);
			this.queue.offer(node);
		}

		@Override
		public boolean hasNext() {
			return this.queue.size() > 0;
		}

		@Override
		public BlockPos next() {
			BlockPos node = this.queue.remove();
			for (Direction direction : BlockConnectNetwork.this.connections.get(node)) {
				BlockPos another = node.offset(direction.getNormal());
				if (this.searched.add(another)) {
					this.queue.offer(another);
				}
			}
			return node;
		}

		public Set<BlockPos> getSearched() {
			return this.searched;
		}
	}
}
