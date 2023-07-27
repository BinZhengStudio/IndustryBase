package cn.bzgzs.industrybase.api.transmit;

import cn.bzgzs.industrybase.api.CapabilityList;
import cn.bzgzs.industrybase.api.network.ApiNetworkManager;
import cn.bzgzs.industrybase.api.network.server.RootSyncPacket;
import cn.bzgzs.industrybase.api.network.server.RootsSyncPacket;
import cn.bzgzs.industrybase.api.network.server.SpeedSyncPacket;
import com.google.common.collect.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;

import java.util.*;

public class TransmitNetwork {
	private final HashMap<BlockPos, LinkedHashSet<BlockPos>> components;
	private final SetMultimap<BlockPos, Direction> connections;
	private final LevelAccessor level;
	private final ArrayDeque<Runnable> tasks;
	private final HashMultiset<BlockPos> powerCollection; // BlockPos 是中心块的坐标，出现个数为连通域总功率的数值
	private final HashMultiset<BlockPos> resistanceCollection;
	private final HashMap<BlockPos, Float> speedCollection;
	private final HashMultiset<BlockPos> machinePower;
	private final HashMultiset<BlockPos> machineResistance;
	private final HashMultimap<BlockPos, ServerPlayer> subscribed; // BlockPos is of root block
	// Only update in client
	private final HashMap<BlockPos, BlockPos> rootCollection;
	private final HashMap<BlockPos, RotateContext> rotateCollection;

	public TransmitNetwork(LevelAccessor level) {
		this.components = new HashMap<>();
		this.connections = Multimaps.newSetMultimap(new HashMap<>(), () -> EnumSet.noneOf(Direction.class));
		this.level = level;
		this.tasks = new ArrayDeque<>();
		this.powerCollection = HashMultiset.create();
		this.resistanceCollection = HashMultiset.create();
		this.speedCollection = new HashMap<>();
		this.rotateCollection = new HashMap<>();
		this.rootCollection = new HashMap<>();
		this.machinePower = HashMultiset.create();
		this.machineResistance = HashMultiset.create();
		this.subscribed = HashMultimap.create();
	}

	public int size(BlockPos pos) {
		return this.components.containsKey(pos) ? this.components.get(pos).size() : 1;
	}

	public BlockPos root(BlockPos pos) {
		if (this.level.isClientSide()) {
			return this.rootCollection.getOrDefault(pos, pos);
		} else {
			return this.components.containsKey(pos) ? this.components.get(pos).iterator().next() : pos;
		}
	}

	public int totalPower(BlockPos pos) {
		BlockPos root = this.root(pos);
		return this.powerCollection.count(root);
	}

	public int totalResistance(BlockPos pos) {
		BlockPos root = this.root(pos);
		return this.resistanceCollection.count(root);
	}

	public float speed(BlockPos pos) {
		BlockPos root = this.root(pos);
		return this.speedCollection.getOrDefault(root, 0.0F);
	}

	public RotateContext getRotateContext(BlockPos pos) {
		BlockPos root = this.root(pos);
		return this.rotateCollection.getOrDefault(root, RotateContext.NULL);
	}

	public float subscribeSpeed(BlockPos pos, ServerPlayer player) {
		BlockPos root = this.root(pos);
		this.subscribed.put(root, player);
		return this.speedCollection.getOrDefault(root, 0.0F);
	}

	public void unsubscribe(BlockPos pos, ServerPlayer player) {
		this.subscribed.remove(pos, player);
	}

	public void addClientSpeed(BlockPos target, BlockPos root, float speed) {
		if (this.level.isClientSide()) {
			this.updateClientRoot(target, root);
			if (speed > 0.0F) {
				this.speedCollection.put(root, speed);
			} else {
				this.speedCollection.remove(root);
				this.rotateCollection.remove(root);
			}
		}
	}

	public void updateClientSpeed(BlockPos root, float speed) {
		if (this.level.isClientSide()) {
			if (speed > 0.0F) {
				this.speedCollection.put(root, speed);
			} else {
				this.speedCollection.remove(root);
				this.rotateCollection.remove(root);
			}
		}
	}

	public void updateClientRoot(BlockPos target, BlockPos root) {
		if (this.level.isClientSide()) {
			if (!target.equals(root)) {
				this.rootCollection.put(target, root);
			} else {
				this.rootCollection.remove(target);
			}
		}
	}

	public void updateClientRoots(Collection<BlockPos> targets, BlockPos root) {
		if (this.level.isClientSide()) {
			targets.forEach(target -> {
				if (!target.equals(root)) {
					this.rootCollection.put(target, root);
				} else {
					this.rootCollection.remove(target);
				}
			});
		}
	}

	public void removeClientRoots(Collection<BlockPos> targets) {
		if (this.level.isClientSide()) {
			targets.forEach(this.rootCollection::remove);
		}
	}

	public boolean shouldSendUnsubscribePacket(BlockPos pos) {
		BlockPos root = this.root(pos);
		if (root.equals(pos)) {
			return !this.rootCollection.containsValue(root);
		}
		return false;
	}

	public void removeClientSubscribe(BlockPos target) {
		if (this.level.isClientSide()) {
			this.speedCollection.remove(target);
			this.rootCollection.remove(target);
			this.rotateCollection.remove(target);
		}
	}

	public int getMachinePower(BlockPos pos) {
		return this.machinePower.count(pos);
	}

	public int setMachinePower(BlockPos pos, int power) {
		int diff = power - this.machinePower.setCount(pos, power);
		if (this.components.containsKey(pos)) {
			BlockPos root = this.root(pos);
			if (diff >= 0) {
				this.powerCollection.add(root, diff);
			} else {
				this.powerCollection.remove(root, -diff);
			}
		}
		this.updateSpeed(pos);
		return diff;
	}

	public int getMachineResistance(BlockPos pos) {
		return this.machineResistance.count(pos);
	}

	public int setMachineResistance(BlockPos pos, int resistance) {
		int diff = resistance - this.machineResistance.setCount(pos, resistance);
		if (this.components.containsKey(pos)) {
			BlockPos root = this.root(pos);
			if (diff >= 0) {
				this.resistanceCollection.add(root, diff);
			} else {
				this.resistanceCollection.remove(root, -diff);
			}
		}
		this.updateSpeed(pos);
		return diff;
	}

	private void updateSpeed(BlockPos pos) {
		BlockPos root = this.root(pos);
		float speed = 0.0F;
		if (this.components.containsKey(pos)) {
			int power = this.powerCollection.count(root);
			int resistance = this.resistanceCollection.count(root);
			if (power > 0 && resistance > 0) {
				speed = (float) power / resistance;
			} else if (power > 0) {
				speed = Float.MAX_VALUE;
			}
		}
		float finalSpeed = speed;
		if (finalSpeed > 0.0F) {
			this.speedCollection.put(root, finalSpeed);
		} else {
			this.speedCollection.remove(root);
		}
		this.subscribed.get(root).forEach(player -> ApiNetworkManager.INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), new SpeedSyncPacket(root, finalSpeed)));
	}

	public void removeBlock(BlockPos pos, Runnable callback) {
		this.tasks.offer(() -> {
			this.setMachinePower(pos, 0);
			this.setMachineResistance(pos, 0);
			for (Direction side : Direction.values()) {
				this.cut(pos, side);
			}
			this.subscribed.removeAll(pos);
			callback.run();
		});
	}

	private void cut(BlockPos node, Direction direction) {
		if (this.connections.remove(node, direction)) {
			BlockPos another = node.relative(direction);
			this.connections.remove(another, direction.getOpposite());
			BFSIterator nodeIterator = new BFSIterator(node);
			BFSIterator anotherIterator = new BFSIterator(another);

			while (nodeIterator.hasNext()) {
				BlockPos next = nodeIterator.next();
				if (!anotherIterator.getSearched().contains(next)) {
					// 互换 iterator，轮询连通域
					BFSIterator iterator = anotherIterator;
					anotherIterator = nodeIterator;
					nodeIterator = iterator;
					continue;
				}
				return; // 如果两个 iterator 存在重复方块（连通域没有断开），则直接退出
			}

			LinkedHashSet<BlockPos> primaryComponent = this.components.get(node);
			LinkedHashSet<BlockPos> secondaryComponent;
			BlockPos primaryNode = primaryComponent.iterator().next();
			LinkedHashSet<BlockPos> searched = nodeIterator.getSearched();

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

				int powerDiff = this.machinePower.count(secondaryNode);
				int resistanceDiff = this.machineResistance.count(secondaryNode);
				this.powerCollection.remove(primaryNode, powerDiff);
				this.resistanceCollection.remove(primaryNode, resistanceDiff);
				this.subscribed.get(primaryNode).forEach(player -> {
					ApiNetworkManager.INSTANCE.send(PacketDistributor.PLAYER.with(() -> player),
							new RootSyncPacket(secondaryNode, secondaryNode));
					this.subscribed.put(secondaryNode, player);
				});
			} else {
				int powerDiff = 0;
				int resistanceDiff = 0;
				for (BlockPos pos : secondaryComponent) {
					this.components.put(pos, secondaryComponent);

					powerDiff += this.machinePower.count(pos);
					resistanceDiff += this.machineResistance.count(pos);
				}
				this.subscribed.get(primaryNode).forEach(player -> {
					ApiNetworkManager.INSTANCE.send(PacketDistributor.PLAYER.with(() -> player),
							new RootsSyncPacket(secondaryComponent, secondaryNode));
					this.subscribed.put(secondaryNode, player);
				});

				this.powerCollection.remove(primaryNode, powerDiff);
				this.resistanceCollection.remove(primaryNode, resistanceDiff);
				this.powerCollection.add(secondaryNode, powerDiff);
				this.resistanceCollection.add(secondaryNode, resistanceDiff);
			}
			if (primaryComponent.size() <= 1) {
				this.components.remove(primaryNode);

				this.powerCollection.setCount(primaryNode, 0);
				this.resistanceCollection.setCount(primaryNode, 0);
				this.subscribed.get(primaryNode).forEach(player -> {
					ApiNetworkManager.INSTANCE.send(PacketDistributor.PLAYER.with(() -> player),
							new RootSyncPacket(primaryNode, primaryNode));
				});
			}

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
			BlockEntity opposite = this.level.getBlockEntity(pos.relative(side));
			boolean flag1 = opposite != null && opposite.getCapability(CapabilityList.MECHANICAL_TRANSMIT, side.getOpposite()).isPresent();
			return flag && flag1;
		}
		return false;
	}

	private void link(BlockPos node, Direction direction) {
		BlockPos secondary = node.immutable();
		if (this.connections.put(secondary, direction)) {
			BlockPos primary = secondary.relative(direction);
			this.connections.put(primary, direction.getOpposite());
			LinkedHashSet<BlockPos> primaryComponent = this.components.get(primary);
			LinkedHashSet<BlockPos> secondaryComponent = this.components.get(secondary);

			int primaryPower = this.machinePower.count(primary);
			int secondaryPower = this.machinePower.count(secondary);
			int primaryResistance = this.machineResistance.count(primary);
			int secondaryResistance = this.machineResistance.count(secondary);

			if (primaryComponent == null && secondaryComponent == null) {
				LinkedHashSet<BlockPos> union = new LinkedHashSet<>();
				this.components.put(secondary, union);
				this.components.put(primary, union);
				union.add(secondary);
				union.add(primary);

				this.powerCollection.setCount(secondary, primaryPower + secondaryPower);
				this.resistanceCollection.setCount(secondary, primaryResistance + secondaryResistance);
				this.updateSpeed(secondary);

				this.subscribed.removeAll(primary).forEach(player -> {
					ApiNetworkManager.INSTANCE.send(PacketDistributor.PLAYER.with(() -> player),
							new RootSyncPacket(primary, secondary));
					this.subscribed.put(secondary, player);
				});
			} else if (primaryComponent == null) {
				BlockPos secondaryNode = secondaryComponent.iterator().next();
				this.components.put(primary, secondaryComponent);
				secondaryComponent.add(primary);

				this.powerCollection.add(secondaryNode, primaryPower);
				this.resistanceCollection.add(secondaryNode, primaryResistance);
				this.updateSpeed(secondaryNode);

				this.subscribed.removeAll(primary).forEach(player -> {
					ApiNetworkManager.INSTANCE.send(PacketDistributor.PLAYER.with(() -> player),
							new RootSyncPacket(primary, secondaryNode));
					this.subscribed.put(secondaryNode, player);
				});
			} else if (secondaryComponent == null) {
				BlockPos primaryNode = primaryComponent.iterator().next();
				this.components.put(secondary, primaryComponent);
				primaryComponent.add(secondary);

				this.powerCollection.add(primaryNode, secondaryPower);
				this.resistanceCollection.add(primaryNode, secondaryResistance);
				this.updateSpeed(primaryNode);

				this.subscribed.removeAll(secondary).forEach(player -> {
					ApiNetworkManager.INSTANCE.send(PacketDistributor.PLAYER.with(() -> player),
							new RootSyncPacket(secondary, primaryNode));
					this.subscribed.put(primaryNode, player);
				});
			} else if (primaryComponent != secondaryComponent) {
				BlockPos primaryNode = primaryComponent.iterator().next();
				BlockPos secondaryNode = secondaryComponent.iterator().next();
				secondaryComponent.forEach(pos -> { // TODO size
					primaryComponent.add(pos);
					this.components.put(pos, primaryComponent);
				});
				this.subscribed.removeAll(secondaryNode).forEach(player -> {
					ApiNetworkManager.INSTANCE.send(PacketDistributor.PLAYER.with(() -> player),
							new RootsSyncPacket(secondaryComponent, primaryNode));
					ApiNetworkManager.INSTANCE.send(PacketDistributor.PLAYER.with(() -> player),
							new SpeedSyncPacket(secondaryNode, 0.0F));
					this.subscribed.put(primaryNode, player);
				});

				this.powerCollection.add(primaryNode, this.powerCollection.setCount(secondaryNode, 0));
				this.resistanceCollection.add(primaryNode, this.resistanceCollection.setCount(secondaryNode, 0));

				this.updateSpeed(primaryNode);
				this.speedCollection.remove(secondaryNode);
			}
		}
	}

	private void serverTick() {
		for (Runnable runnable = this.tasks.poll(); runnable != null; runnable = this.tasks.poll()) {
			runnable.run();
		}
	}

	private void clientTick() {
		this.speedCollection.forEach((pos, speed) -> {
			RotateContext context = this.rotateCollection.computeIfAbsent(pos, blockPos -> new RotateContext(0.0F, 0.0F));
			float degree = context.getDegree();
			context.setOldDegree(degree);
			context.setDegree(degree + (speed * 18.0F));
		});
	}

	public class BFSIterator implements Iterator<BlockPos> {
		private final LinkedHashSet<BlockPos> searched = new LinkedHashSet<>();
		private final ArrayDeque<BlockPos> queue = new ArrayDeque<>();

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

	public static class RotateContext {
		public static final RotateContext NULL = new RotateContext(0.0F, 0.0F);
		private float oldDegree;
		private float degree;

		public RotateContext(float oldDegree, float degree) {
			this.oldDegree = oldDegree;
			this.degree = degree;
		}

		public float getOldDegree() {
			return this.oldDegree;
		}

		public void setOldDegree(float oldDegree) {
			this.oldDegree = oldDegree % 360.0F;
		}

		public float getDegree() {
			return this.degree;
		}

		public void setDegree(float degree) {
			this.degree = degree % 360.0F;
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
				if (event.side.isClient()) {
					get(event.level).clientTick();
				} else {
					get(event.level).serverTick();
				}
			}
		}
	}
}
