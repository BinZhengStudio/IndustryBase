package cn.bzgzs.industrybase.api.util;

import cn.bzgzs.industrybase.api.CapabilityList;
import cn.bzgzs.industrybase.api.event.TransmitNetworkEvent;
import com.google.common.collect.*;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.*;

public class TransmitNetwork {
	private final Map<BlockPos, Set<BlockPos>> components;
	private final SetMultimap<BlockPos, Direction> connections;
	private final LevelAccessor level;
	private final Queue<Runnable> tasks;
	private final Multiset<BlockPos> powerCollection; // BlockPos 是中心块的坐标，出现个数为连通域总功率的数值
	private final Multiset<BlockPos> resistanceCollection;
	private final Map<BlockPos, Double> speedCollection;
	private final Map<BlockPos, BlockPos> rootCollection;
	private final Map<BlockPos, RotateContext> rotateCollection; // Only update in client
	private final Multiset<BlockPos> machinePower;
	private final Multiset<BlockPos> machineResistance;

	public TransmitNetwork(LevelAccessor level) {
		this.components = new HashMap<>();
		this.connections = Multimaps.newSetMultimap(new HashMap<>(), () -> EnumSet.noneOf(Direction.class));
		this.level = level;
		this.tasks = Queues.newArrayDeque();
		this.powerCollection = HashMultiset.create();
		this.resistanceCollection = HashMultiset.create();
		this.speedCollection = new HashMap<>();
		this.rotateCollection = new HashMap<>();
		this.rootCollection = new HashMap<>();
		this.machinePower = HashMultiset.create();
		this.machineResistance = HashMultiset.create();
	}

	public int size(BlockPos pos) {
		return this.components.containsKey(pos) ? this.components.get(pos).size() : 1;
	}

	public BlockPos root(BlockPos pos) {
		return this.level.isClientSide() ? this.rootCollection.getOrDefault(pos, pos) : this.components.getOrDefault(pos, ImmutableSet.of(pos.immutable())).iterator().next();
	}

	public int totalPower(BlockPos pos) {
		BlockPos root = this.root(pos);
		return this.powerCollection.count(root);
	}

	public int totalResistance(BlockPos pos) {
		BlockPos root = this.root(pos);
		return this.resistanceCollection.count(root);
	}

	public double speed(BlockPos pos) {
		BlockPos root = this.root(pos);
		return this.speedCollection.getOrDefault(root, 0.0D);
	}

	public RotateContext getRotateContext(BlockPos pos) {
		BlockPos root = this.root(pos);
		return this.rotateCollection.getOrDefault(root, new RotateContext(0.0D, 0.0D));
	}

	public Map<BlockPos, Double> getSpeedCollection() {
		return new HashMap<>(this.speedCollection);
	}

	public void updateSpeedCollection(Map<BlockPos, Double> dataToUpdate, Set<BlockPos> deleted) {
		if (this.level.isClientSide()) {
			this.speedCollection.putAll(dataToUpdate);
			deleted.forEach(pos -> {
				this.speedCollection.remove(pos);
				this.rotateCollection.remove(pos);
			});
		}
	}

	public Map<BlockPos, BlockPos> getRootCollection() {
		return new HashMap<>(this.rootCollection);
	}

	public void updateRootCollection(Map<BlockPos, BlockPos> dataToUpdate, Set<BlockPos> deleted) {
		if (this.level.isClientSide()) {
			this.rootCollection.putAll(dataToUpdate);
			deleted.forEach(this.rootCollection::remove);
		}
	}

	public int getMachinePower(BlockPos pos) {
		return this.machinePower.count(pos);
	}

	public int setMachinePower(BlockPos pos, int power) {
		int diff = power - this.machinePower.setCount(pos, power);
		if (diff >= 0) {
			this.powerCollection.add(this.root(pos), diff);
		} else {
			this.powerCollection.remove(this.root(pos), -diff);
		}
		this.updateSpeed(pos);
		return diff;
	}

	public int getMachineResistance(BlockPos pos) {
		return this.machineResistance.count(pos);
	}

	public int setMachineResistance(BlockPos pos, int resistance) {
		int diff = resistance - this.machineResistance.setCount(pos, resistance);
		if (diff >= 0) {
			this.resistanceCollection.add(this.root(pos), diff);
		} else {
			this.resistanceCollection.remove(this.root(pos), -diff);
		}
		this.updateSpeed(pos);
		return diff;
	}

	private void updateSpeed(BlockPos pos) {
		BlockPos root = this.root(pos);
		if (this.components.containsKey(pos)) {
			int power = this.powerCollection.count(root);
			int resistance = this.resistanceCollection.count(root);
			if (power > 0 && resistance > 0) {
				double speed = (double) this.powerCollection.count(root) / this.resistanceCollection.count(root);
				this.speedCollection.put(root, speed);
				Map<BlockPos, Double> updated = new HashMap<>();
				updated.put(root, speed);
				MinecraftForge.EVENT_BUS.post(new TransmitNetworkEvent.UpdateSpeedEvent(this.level, updated, new HashSet<>()));
			} else if (power > 0) {
				this.speedCollection.put(root, Double.MAX_VALUE);
				Map<BlockPos, Double> updated = new HashMap<>();
				updated.put(root, Double.MAX_VALUE);
				MinecraftForge.EVENT_BUS.post(new TransmitNetworkEvent.UpdateSpeedEvent(this.level, updated, new HashSet<>()));
			} else {
				this.speedCollection.remove(root);
				MinecraftForge.EVENT_BUS.post(new TransmitNetworkEvent.UpdateSpeedEvent(this.level, new HashMap<>(), Set.of(root)));
			}
		} else if (this.speedCollection.containsKey(root)) {
			this.speedCollection.remove(root);
			MinecraftForge.EVENT_BUS.post(new TransmitNetworkEvent.UpdateSpeedEvent(this.level, new HashMap<>(), Set.of(root)));
		}
	}

	public void removeBlock(BlockPos pos, Runnable callback) {
		this.tasks.offer(() -> {
			for (Direction side : Direction.values()) {
				this.cut(pos, side);
			}
			callback.run();
		});
	}

	private void cut(BlockPos node, Direction direction) {
		if (this.connections.remove(node, direction)) {
			BlockPos another = node.offset(direction.getNormal());
			this.connections.remove(another, direction.getOpposite());
			BFSIterator nodeIterator = new BFSIterator(node);
			BFSIterator anotherIterator = new BFSIterator(another);

			while (nodeIterator.hasNext()) {
				BlockPos next = nodeIterator.next();
				if (!anotherIterator.getSearched().contains(next)) {
					// 互换 iterator
					BFSIterator iterator = anotherIterator;
					anotherIterator = nodeIterator;
					nodeIterator = iterator;
					continue;
				}
				return; // 如果两个 iterator 存在重复方块（连通域没有断开），则直接退出
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
				int power = this.powerCollection.remove(primaryNode, powerDiff) - powerDiff;
				int resistance = this.resistanceCollection.remove(primaryNode, resistanceDiff) - resistanceDiff;
				if (power > 0) {
					updatedPower.setCount(primaryNode, power);
				} else {
					deletedPower.add(primaryNode);
				}
				if (resistance > 0) {
					updatedPower.setCount(primaryNode, resistance);
				} else {
					deletedPower.add(primaryNode);
				}

				this.rootCollection.remove(secondaryNode);
				deletedRoot.add(secondaryNode);
			} else {
				int powerDiff = 0;
				int resistanceDiff = 0;
				for (BlockPos pos : secondaryComponent) {
					this.components.put(pos, secondaryComponent);

					powerDiff += this.machinePower.count(pos);
					resistanceDiff += this.machineResistance.count(pos);
					// 将原先从主连通域分离的映射移到子连通域
					this.rootCollection.put(pos, secondaryNode);
					updatedRoot.put(pos, secondaryNode);
				}
				int primaryPower = this.powerCollection.remove(primaryNode, powerDiff) - powerDiff;
				int primaryResistance = this.resistanceCollection.remove(primaryNode, resistanceDiff) - resistanceDiff;
				if (primaryPower > 0) {
					updatedPower.setCount(primaryNode, primaryPower);
				} else {
					deletedPower.add(primaryNode);
				}
				if (primaryResistance > 0) {
					updatedPower.setCount(primaryNode, primaryResistance);
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

	public void addOrChangeBlock(BlockPos pos, Runnable callback) {
		this.tasks.offer(() -> {
			for (Direction side : Direction.values()) {
				if (this.hasMechanicalConnection(pos, side)) { // 某个方向上有与其他传动设备连接
					this.link(pos, side);
				} else {
					this.cut(pos, side);
				}
			}
			callback.run();
		});
	}

	@SuppressWarnings("deprecation")
	private boolean hasMechanicalConnection(BlockPos pos, Direction side) {
		if (this.level.isAreaLoaded(pos, 0)) {
			BlockEntity blockEntity = this.level.getBlockEntity(pos);
			boolean flag = blockEntity != null && blockEntity.getCapability(CapabilityList.MECHANICAL_TRANSMIT, side).isPresent();
			BlockEntity opposite = this.level.getBlockEntity(pos.offset(side.getNormal()));
			boolean flag1 = opposite != null && opposite.getCapability(CapabilityList.MECHANICAL_TRANSMIT, side.getOpposite()).isPresent();
			return flag && flag1;
		}
		return false;
	}

	private void link(BlockPos node, Direction direction) {
		BlockPos secondary = node.immutable();
		if (this.connections.put(secondary, direction)) {
			BlockPos primary = secondary.offset(direction.getNormal());
			this.connections.put(primary, direction.getOpposite());
			Set<BlockPos> primaryComponent = this.components.get(primary);
			Set<BlockPos> secondaryComponent = this.components.get(secondary);

			int primaryPower = this.machinePower.count(primary);
			int secondaryPower = this.machinePower.count(secondary);
			int primaryResistance = this.machineResistance.count(primary);
			int secondaryResistance = this.machineResistance.count(secondary);

			Multiset<BlockPos> updatedPower = HashMultiset.create();
			Set<BlockPos> deletedPower = new HashSet<>();
			Multiset<BlockPos> updatedResistance = HashMultiset.create();
			Set<BlockPos> deletedResistance = new HashSet<>();
			Map<BlockPos, BlockPos> updatedRoot = new HashMap<>();
			if (primaryComponent == null && secondaryComponent == null) {
				Set<BlockPos> union = new LinkedHashSet<>();
				this.components.put(secondary, union);
				this.components.put(primary, union);
				union.add(secondary);
				union.add(primary);

				this.powerCollection.setCount(secondary, primaryPower + secondaryPower);
				updatedPower.setCount(secondary, primaryPower + secondaryPower);
				this.resistanceCollection.setCount(secondary, primaryResistance + secondaryResistance);
				updatedResistance.setCount(secondary, primaryResistance + secondaryResistance);
				this.updateSpeed(secondary);

				this.rootCollection.put(secondary, secondary);
				updatedRoot.put(secondary, secondary);

				this.rootCollection.put(primary, secondary);
				updatedRoot.put(primary, secondary);
			} else if (primaryComponent == null) {
				BlockPos secondaryNode = secondaryComponent.iterator().next();
				this.components.put(primary, secondaryComponent);
				secondaryComponent.add(primary);

				updatedPower.setCount(secondaryNode, this.powerCollection.add(secondaryNode, primaryPower) + primaryPower);
				updatedResistance.setCount(secondaryNode, this.resistanceCollection.add(secondaryNode, primaryResistance) + primaryResistance);
				this.updateSpeed(secondaryNode);

				this.rootCollection.put(primary, secondaryNode);
				updatedRoot.put(primary, secondaryNode);
			} else if (secondaryComponent == null) {
				BlockPos primaryNode = primaryComponent.iterator().next();
				this.components.put(secondary, primaryComponent);
				primaryComponent.add(secondary);

				updatedPower.setCount(primaryNode, this.powerCollection.add(primaryNode, secondaryPower) + secondaryPower);
				updatedResistance.setCount(primaryNode, this.resistanceCollection.add(primaryNode, secondaryResistance) + secondaryResistance);
				this.updateSpeed(primaryNode);

				this.rootCollection.put(secondary, primaryNode);
				updatedRoot.put(secondary, primaryNode);
			} else if (primaryComponent != secondaryComponent) {
				BlockPos primaryNode = primaryComponent.iterator().next();
				BlockPos secondaryNode = secondaryComponent.iterator().next();
				Set<BlockPos> union = new LinkedHashSet<>(Sets.union(primaryComponent, secondaryComponent));
				union.forEach(pos -> {
					this.components.put(pos, union);
					this.rootCollection.put(pos, primaryNode);
					updatedRoot.put(pos, primaryNode);
				});

				int powerDiff = this.powerCollection.count(secondaryNode);
				this.powerCollection.setCount(secondaryNode, 0);
				deletedPower.add(secondaryNode);
				updatedPower.setCount(primaryNode, this.powerCollection.add(primaryNode, powerDiff) + powerDiff);

				int resistanceDiff = this.resistanceCollection.count(secondaryNode);
				this.resistanceCollection.setCount(secondaryNode, 0);
				deletedResistance.add(secondaryNode);
				updatedResistance.setCount(primaryNode, this.resistanceCollection.add(primaryNode, resistanceDiff) + resistanceDiff);

				this.updateSpeed(primaryNode);
				this.updateSpeed(secondaryNode);
			}
			MinecraftForge.EVENT_BUS.post(new TransmitNetworkEvent.UpdatePowerEvent(this.level, updatedPower, deletedPower));
			MinecraftForge.EVENT_BUS.post(new TransmitNetworkEvent.UpdateResistanceEvent(this.level, updatedResistance, deletedResistance));
			MinecraftForge.EVENT_BUS.post(new TransmitNetworkEvent.UpdateRootEvent(this.level, updatedRoot, new HashSet<>()));
		}
	}

	private void serverTick() {
		for (Runnable runnable = this.tasks.poll(); runnable != null; runnable = this.tasks.poll()) {
			runnable.run();
		}
	}

	private void clientTick() {
		this.speedCollection.forEach((pos, speed) -> {
			RotateContext context = this.rotateCollection.computeIfAbsent(pos, blockPos -> new RotateContext(0.0D, 0.0D));
			double degree = context.getDegree();
			context.setOldDegree(degree);
			context.setDegree(degree + (speed * 18.0D));
		});
	}

	public class BFSIterator implements Iterator<BlockPos> {
		private final Set<BlockPos> searched = new LinkedHashSet<>();
		private final Queue<BlockPos> queue = new ArrayDeque<>();

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
			for (Direction direction : TransmitNetwork.this.connections.get(node)) {
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

	public static class RotateContext {
		private double oldDegree;
		private double degree;

		public RotateContext(double oldDegree, double degree) {
			this.oldDegree = oldDegree;
			this.degree = degree;
		}

		public double getOldDegree() {
			return this.oldDegree;
		}

		public void setOldDegree(double oldDegree) {
			this.oldDegree = oldDegree % 360.0D;
		}

		public double getDegree() {
			return this.degree;
		}

		public void setDegree(double degree) {
			this.degree = degree % 360.0D;
		}
	}

	@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE)
	public static class Manager {
		private static final Map<LevelAccessor, TransmitNetwork> INSTANCES = new IdentityHashMap<>();

		public static TransmitNetwork get(LevelAccessor level) {
			return INSTANCES.computeIfAbsent(level, TransmitNetwork::new);
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

		@SubscribeEvent
		public static void onClientTick(TickEvent.ClientTickEvent event) {
			if (event.phase == TickEvent.Phase.START) {
				Optional.ofNullable(Minecraft.getInstance().level).ifPresent(level -> get(level).clientTick());
			}
		}
	}
}
