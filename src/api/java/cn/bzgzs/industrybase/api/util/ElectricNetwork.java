package cn.bzgzs.industrybase.api.util;

import cn.bzgzs.industrybase.api.CapabilityList;
import com.google.common.collect.Multimaps;
import com.google.common.collect.Queues;
import com.google.common.collect.SetMultimap;
import com.google.common.collect.Sets;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.*;

public class ElectricNetwork {
	private final Map<BlockPos, Set<BlockPos>> components;
	private final SetMultimap<BlockPos, Direction> sideConn;
	private final SetMultimap<BlockPos, BlockPos> wireConn;
	private final LevelAccessor level;
	private final Queue<Runnable> tasks;
	private final Map<BlockPos, Double> outputCollection;
	private final Map<BlockPos, Double> inputCollection;
//	private final Map<BlockPos, BlockPos> rootCollection; // TODO
	private final Map<BlockPos, Double> machineOutput;
	private final Map<BlockPos, Double> machineInput;
	private final SetMultimap<BlockPos, Direction> machinesFE;

	public ElectricNetwork(LevelAccessor level) {
		this.components = new HashMap<>();
		this.sideConn = Multimaps.newSetMultimap(new HashMap<>(), () -> EnumSet.noneOf(Direction.class));
		this.wireConn = Multimaps.newSetMultimap(new HashMap<>(), HashSet::new);
		this.level = level;
		this.tasks = Queues.newArrayDeque();
		this.outputCollection = new HashMap<>();
		this.inputCollection = new HashMap<>();
//		this.rootCollection = new HashMap<>();
		this.machineOutput = new HashMap<>();
		this.machineInput = new HashMap<>();
		this.machinesFE = Multimaps.newSetMultimap(new HashMap<>(), () -> EnumSet.noneOf(Direction.class));
	}

	public int size(BlockPos pos) {
		return this.components.containsKey(pos) ? this.components.get(pos).size() : 1;
	}

	public BlockPos root(BlockPos pos) {
//		return this.level.isClientSide() ? this.rootCollection.getOrDefault(pos, pos) : this.components.getOrDefault(pos, ImmutableSet.of(pos.immutable())).iterator().next();
		return this.components.containsKey(pos) ? this.components.get(pos).iterator().next() : pos;
	}

	public double totalOutput(BlockPos pos) {
		BlockPos root = this.root(pos);
		return this.outputCollection.get(root);
	}

	public double totalInput(BlockPos pos) {
		BlockPos root = this.root(pos);
		return this.inputCollection.get(root);
	}

	public double getMachineOutput(BlockPos pos) {
		return this.machineOutput.get(pos);
	}

	public void setMachineOutput(BlockPos pos, double power) {
		if (power > 0) {
			this.machineOutput.put(pos, power);
		} else {
			this.machineOutput.remove(pos);
		}
	}

	public double getMachineInput(BlockPos pos) {
		return this.machineInput.get(pos);
	}

	public void setMachineInput(BlockPos pos, double power) {
		if (power > 0) {
			this.machineInput.put(pos, power);
		} else {
			this.machineInput.remove(pos);
		}
	}

	public void removeBlock(BlockPos pos, Runnable callback) {
		this.tasks.offer(() -> {
			for (Direction side : Direction.values()) {
				this.cutSide(pos, side);
			}
			for (BlockPos another : this.wireConn.get(pos)) {
				this.cutWire(pos, another);
			}
			callback.run();
		});
	}

	private void cutSide(BlockPos node, Direction direction) {
		if (this.sideConn.remove(node, direction)) {
			BlockPos another = node.offset(direction.getNormal());
			this.sideConn.remove(another, direction.getOpposite());
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

			Set<BlockPos> primaryComponent = this.components.get(node);
			Set<BlockPos> secondaryComponent;
			BlockPos primaryNode = primaryComponent.iterator().next();
			Set<BlockPos> searched = nodeIterator.getSearched();

			if (searched.contains(primaryNode)) {
				secondaryComponent = new LinkedHashSet<>(Sets.difference(primaryComponent, searched));
				primaryComponent.retainAll(searched);
			} else {
				secondaryComponent = searched;
				primaryComponent.removeAll(searched);
			}

			BlockPos secondaryNode = secondaryComponent.iterator().next();
			if (secondaryComponent.size() <= 1) {
				this.components.remove(secondaryNode);

				double outputDiff = this.machineOutput.getOrDefault(secondaryNode, 0.0D);
				double inputDiff = this.machineInput.getOrDefault(secondaryNode, 0.0D);
				double outputOld = this.outputCollection.getOrDefault(primaryNode, 0.0D);
				double inputOld = this.inputCollection.getOrDefault(primaryNode, 0.0D);
				this.outputCollection.put(primaryNode, outputOld - outputDiff);
				this.inputCollection.put(primaryNode, inputOld - inputDiff);

//				this.rootCollection.remove(secondaryNode);
			} else {
				double outputDiff = 0.0D;
				double inputDiff = 0.0D;
				for (BlockPos pos : secondaryComponent) {
					this.components.put(pos, secondaryComponent);

					outputDiff += this.machineOutput.get(pos);
					inputDiff += this.machineInput.get(pos);

//					this.rootCollection.put(pos, secondaryNode);
				}
				double outputOld = this.outputCollection.getOrDefault(primaryNode, 0.0D);
				double inputOld = this.inputCollection.getOrDefault(primaryNode, 0.0D);
				this.outputCollection.put(primaryNode, outputOld - outputDiff);
				this.inputCollection.put(primaryNode, inputOld - inputDiff);
			}
			if (primaryComponent.size() <= 1) {
				this.components.remove(primaryNode);

				this.outputCollection.remove(primaryNode);
				this.inputCollection.remove(primaryNode);
//				this.rootCollection.remove(primaryNode);
			}
		}
	}

	private void cutWire(BlockPos pos, BlockPos another) {
	}

	public void addOrChangeBlock(BlockPos pos, Runnable callback) {
		this.tasks.offer(() -> {
			for (Direction side : Direction.values()) {
				if (this.hasElectricalConnection(pos, side)) { // 某个方向上有与其他传动设备连接
					this.link(pos, side);
				} else {
					this.cutSide(pos, side);
				}
			}
			callback.run();
		});
	}

	@SuppressWarnings("deprecation")
	private boolean hasElectricalConnection(BlockPos pos, Direction side) {
		if (this.level.isAreaLoaded(pos, 0)) {
			BlockEntity blockEntity = this.level.getBlockEntity(pos);
			boolean flag = blockEntity != null && blockEntity.getCapability(CapabilityList.ELECTRIC_POWER, side).isPresent();
			BlockEntity opposite = this.level.getBlockEntity(pos.offset(side.getNormal()));
			boolean flag1 = opposite != null && opposite.getCapability(CapabilityList.ELECTRIC_POWER, side.getOpposite()).isPresent();
			return flag && flag1;
		}
		return false;
	}

	private void link(BlockPos node, Direction direction) {
		BlockPos secondary = node.immutable();
		if (this.sideConn.put(secondary, direction)) {
			BlockPos primary = secondary.offset(direction.getNormal());
			this.sideConn.put(primary, direction.getOpposite());
			Set<BlockPos> primaryComponent = this.components.get(primary);
			Set<BlockPos> secondaryComponent = this.components.get(secondary);

			double primaryOutput = this.machineOutput.getOrDefault(primary, 0.0D);
			double secondaryOutput = this.machineOutput.getOrDefault(secondary, 0.0D);
			double primaryInput = this.machineInput.getOrDefault(primary, 0.0D);
			double secondaryInput = this.machineInput.getOrDefault(secondary, 0.0D);

			if (primaryComponent == null && secondaryComponent == null) {
				Set<BlockPos> union = new LinkedHashSet<>();
				this.components.put(secondary, union);
				this.components.put(primary, union);
				union.add(secondary);
				union.add(primary);

				this.outputCollection.put(secondary, primaryOutput + secondaryOutput);
				this.inputCollection.put(secondary, primaryInput + secondaryInput);

//				this.rootCollection.put(secondary, secondary);
//				this.rootCollection.put(primary, secondary);
			} else if (primaryComponent == null) {
				BlockPos secondaryNode = secondaryComponent.iterator().next();
				this.components.put(primary, secondaryComponent);
				secondaryComponent.add(primary);

				double outputOld = this.outputCollection.getOrDefault(secondaryNode, 0.0D);
				this.outputCollection.put(secondaryNode, outputOld + primaryOutput);
				double inputOld = this.inputCollection.getOrDefault(secondaryNode, 0.0D);
				this.inputCollection.put(secondaryNode, inputOld + primaryInput);

//				this.rootCollection.put(primary, secondaryNode);
			} else if (secondaryComponent == null) {
				BlockPos primaryNode = primaryComponent.iterator().next();
				this.components.put(secondary, primaryComponent);
				primaryComponent.add(secondary);

				double outputOld = this.outputCollection.getOrDefault(primaryNode, 0.0D);
				this.outputCollection.put(primaryNode, outputOld + secondaryOutput);
				double inputOld = this.inputCollection.getOrDefault(primaryNode, 0.0D);
				this.inputCollection.put(primaryNode, inputOld + secondaryInput);

//				this.rootCollection.put(secondary, primaryNode);
			} else if (primaryComponent != secondaryComponent) {
				BlockPos primaryNode = primaryComponent.iterator().next();
				BlockPos secondaryNode = secondaryComponent.iterator().next();
				Set<BlockPos> union = new LinkedHashSet<>(Sets.union(primaryComponent, secondaryComponent));
				union.forEach(pos -> {
					this.components.put(pos, union);
//					this.rootCollection.put(pos, primaryNode);
				});

				double outputDiff = this.outputCollection.getOrDefault(secondaryNode, 0.0D);
				double outputOld = this.outputCollection.getOrDefault(primaryNode, 0.0D);
				this.outputCollection.remove(secondaryNode);
				this.outputCollection.put(primaryNode, outputOld + outputDiff);

				double inputDiff = this.inputCollection.getOrDefault(secondaryNode, 0.0D);
				double inputOld = this.inputCollection.getOrDefault(primaryNode, 0.0D);
				this.inputCollection.remove(secondaryNode);
				this.inputCollection.put(primaryNode, inputOld + inputDiff);
			}
		}
	}

	private void serverTick() {
		for (Runnable runnable = this.tasks.poll(); runnable != null; runnable = this.tasks.poll()) {
			runnable.run();
		}
	}

	public class BFSIterator implements Iterator<BlockPos> {
		private final Set<BlockPos> searched = Sets.newLinkedHashSet();
		private final Queue<BlockPos> queue = Queues.newArrayDeque();

		public BFSIterator(BlockPos node) {
			node = node.immutable();
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
			for (Direction direction : ElectricNetwork.this.sideConn.get(node)) {
				BlockPos another = node.offset(direction.getNormal());
				if (this.searched.add(another)) {
					this.queue.offer(another);
				}
			}
			for (BlockPos another : ElectricNetwork.this.wireConn.get(node)) {
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

	@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE)
	public static class Manager {
		private static final Map<LevelAccessor, ElectricNetwork> INSTANCES = new IdentityHashMap<>();

		public static ElectricNetwork get(LevelAccessor level) {
			return INSTANCES.computeIfAbsent(level, ElectricNetwork::new);
		}

		@SubscribeEvent
		public static void onUnload(LevelEvent.Unload event) {
			INSTANCES.remove(event.getLevel());
		}

		@SubscribeEvent
		public static void onLevelTick(TickEvent.LevelTickEvent event) {
			if (event.phase == TickEvent.Phase.START) {
				if (event.side.isServer()) {
					get(event.level).serverTick();
				}
			}
		}
	}
}
