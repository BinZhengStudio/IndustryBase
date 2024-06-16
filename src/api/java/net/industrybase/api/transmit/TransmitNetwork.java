package net.industrybase.api.transmit;

import net.industrybase.api.CapabilityList;
import net.industrybase.api.network.ApiNetworkManager;
import net.industrybase.api.network.server.RootSyncPacket;
import net.industrybase.api.network.server.RootsSyncPacket;
import net.industrybase.api.network.server.SpeedSyncPacket;
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
	private final HashMultiset<BlockPos> totalPower; // BlockPos 是中心块的坐标，出现个数为连通域总功率的数值
	private final HashMultiset<BlockPos> totalResistance;
	private final HashMap<BlockPos, Float> speeds;
	private final HashMultiset<BlockPos> machinePower;
	private final HashMultiset<BlockPos> machineResistance;
	private final HashMultimap<BlockPos, ServerPlayer> subscribes; // BlockPos is of root block
	// Only update in client
	private final HashMap<BlockPos, BlockPos> roots;
	private final HashMap<BlockPos, RotateContext> rotates;

	public TransmitNetwork(LevelAccessor level) {
		this.components = new HashMap<>();
		this.connections = Multimaps.newSetMultimap(new HashMap<>(), () -> EnumSet.noneOf(Direction.class));
		this.level = level;
		this.tasks = new ArrayDeque<>();
		this.totalPower = HashMultiset.create();
		this.totalResistance = HashMultiset.create();
		this.speeds = new HashMap<>();
		this.rotates = new HashMap<>();
		this.roots = new HashMap<>();
		this.machinePower = HashMultiset.create();
		this.machineResistance = HashMultiset.create();
		this.subscribes = HashMultimap.create();
	}

	public int size(BlockPos pos) {
		return this.components.containsKey(pos) ? this.components.get(pos).size() : 1;
	}

	public BlockPos root(BlockPos pos) {
		if (this.level.isClientSide()) {
			return this.roots.getOrDefault(pos, pos);
		} else {
			return this.components.containsKey(pos) ? this.components.get(pos).getFirst() : pos;
		}
	}

	public int totalPower(BlockPos pos) {
		BlockPos root = this.root(pos);
		return this.totalPower.count(root);
	}

	public int totalResistance(BlockPos pos) {
		BlockPos root = this.root(pos);
		return this.totalResistance.count(root);
	}

	public float speed(BlockPos pos) {
		BlockPos root = this.root(pos);
		return this.speeds.getOrDefault(root, 0.0F);
	}

	public RotateContext getRotateContext(BlockPos pos) {
		BlockPos root = this.root(pos);
		return this.rotates.getOrDefault(root, RotateContext.NULL);
	}

	public float subscribeSpeed(BlockPos pos, ServerPlayer player) {
		BlockPos root = this.root(pos);
		this.subscribes.put(root, player);
		return this.speeds.getOrDefault(root, 0.0F);
	}

	public void unsubscribe(BlockPos pos, ServerPlayer player) {
		this.subscribes.remove(pos, player);
	}

	public void addClientSpeed(BlockPos target, BlockPos root, float speed) {
		if (this.level.isClientSide()) {
			this.updateClientRoot(target, root);
			if (speed > 0.0F) {
				this.speeds.put(root, speed);
			} else {
				this.speeds.remove(root);
				this.rotates.remove(root);
			}
		}
	}

	public void updateClientSpeed(BlockPos root, float speed) {
		if (this.level.isClientSide()) {
			if (speed > 0.0F) {
				this.speeds.put(root, speed);
			} else {
				this.speeds.remove(root);
				this.rotates.remove(root);
			}
		}
	}

	public void updateClientRoot(BlockPos target, BlockPos root) {
		if (this.level.isClientSide()) {
			if (!target.equals(root)) {
				this.roots.put(target, root);
			} else {
				this.roots.remove(target);
			}
		}
	}

	public void updateClientRoots(Collection<BlockPos> targets, BlockPos root) {
		if (this.level.isClientSide()) {
			targets.forEach(target -> {
				if (!target.equals(root)) {
					this.roots.put(target, root);
				} else {
					this.roots.remove(target);
				}
			});
		}
	}

	public void removeClientRoots(Collection<BlockPos> targets) {
		if (this.level.isClientSide()) {
			targets.forEach(this.roots::remove);
		}
	}

	public boolean shouldSendUnsubscribePacket(BlockPos pos) {
		// 如果当前网络还有其他非 root 方块，返回 false
		return !this.roots.containsValue(this.root(pos));
	}

	public void removeClientSubscribe(BlockPos target) {
		if (this.level.isClientSide()) {
			this.speeds.remove(target);
			this.roots.remove(target);
			this.rotates.remove(target);
		}
	}

	public int getMachinePower(BlockPos pos) {
		return this.machinePower.count(pos);
	}

	public int setMachinePower(BlockPos pos, int power) {
		if (this.machinePower.count(pos) == power) return 0;
		int diff = power - this.machinePower.setCount(pos, power);
		if (this.components.containsKey(pos)) {
			BlockPos root = this.root(pos);
			if (diff >= 0) {
				this.totalPower.add(root, diff);
			} else {
				this.totalPower.remove(root, -diff);
			}
		}
		this.updateSpeed(pos);
		return diff;
	}

	public int getMachineResistance(BlockPos pos) {
		return this.machineResistance.count(pos);
	}

	public int setMachineResistance(BlockPos pos, int resistance) {
		if (this.machineResistance.count(pos) == resistance) return 0;
		int diff = resistance - this.machineResistance.setCount(pos, resistance);
		if (this.components.containsKey(pos)) {
			BlockPos root = this.root(pos);
			if (diff >= 0) {
				this.totalResistance.add(root, diff);
			} else {
				this.totalResistance.remove(root, -diff);
			}
		}
		this.updateSpeed(pos);
		return diff;
	}

	private void updateSpeed(BlockPos pos) {
		BlockPos root = this.root(pos);
		float speed = 0.0F;
		if (this.components.containsKey(pos)) {
			int power = this.totalPower.count(root);
			int resistance = this.totalResistance.count(root);
			if (power > 0 && resistance > 0) {
				speed = (float) power / resistance;
			} else if (power > 0) {
				speed = Float.MAX_VALUE;
			}
		}
		float finalSpeed = speed;
		if (finalSpeed > 0.0F) {
			this.speeds.put(root, finalSpeed);
		} else {
			this.speeds.remove(root);
		}
		this.subscribes.get(root).forEach(player ->
				ApiNetworkManager.INSTANCE.send(new SpeedSyncPacket(root, finalSpeed),
						PacketDistributor.PLAYER.with(player)));
	}

	public void removeBlock(BlockPos pos, Runnable callback) {
		this.tasks.offer(() -> {
			this.setMachinePower(pos, 0);
			this.setMachineResistance(pos, 0);
			for (Direction side : Direction.values()) {
				this.cut(pos, side);
			}
			this.subscribes.removeAll(pos);
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

				int powerDiff = this.machinePower.count(secondaryNode);
				int resistanceDiff = this.machineResistance.count(secondaryNode);
				this.totalPower.remove(primaryNode, powerDiff);
				this.totalResistance.remove(primaryNode, resistanceDiff);
				this.subscribes.get(primaryNode).forEach(player -> {
					ApiNetworkManager.INSTANCE.send(new RootSyncPacket(secondaryNode, secondaryNode),
							PacketDistributor.PLAYER.with(player));
					this.subscribes.put(secondaryNode, player);
				});
			} else {
				int powerDiff = 0;
				int resistanceDiff = 0;
				for (BlockPos pos : secondaryComponent) {
					this.components.put(pos, secondaryComponent);

					powerDiff += this.machinePower.count(pos);
					resistanceDiff += this.machineResistance.count(pos);
				}
				this.subscribes.get(primaryNode).forEach(player -> {
					ApiNetworkManager.INSTANCE.send(new RootsSyncPacket(secondaryComponent, secondaryNode),
							PacketDistributor.PLAYER.with(player));
					this.subscribes.put(secondaryNode, player);
				});

				this.totalPower.remove(primaryNode, powerDiff);
				this.totalResistance.remove(primaryNode, resistanceDiff);
				this.totalPower.add(secondaryNode, powerDiff);
				this.totalResistance.add(secondaryNode, resistanceDiff);
			}
			if (primaryComponent.size() <= 1) {
				this.components.remove(primaryNode);

				this.totalPower.setCount(primaryNode, 0);
				this.totalResistance.setCount(primaryNode, 0);
				this.subscribes.get(primaryNode).forEach(player -> {
					ApiNetworkManager.INSTANCE.send(new RootSyncPacket(primaryNode, primaryNode),
							PacketDistributor.PLAYER.with(player));
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

				this.totalPower.setCount(secondary, primaryPower + secondaryPower);
				this.totalResistance.setCount(secondary, primaryResistance + secondaryResistance);
				this.updateSpeed(secondary);

				this.subscribes.removeAll(primary).forEach(player -> {
					ApiNetworkManager.INSTANCE.send(new RootSyncPacket(primary, secondary),
							PacketDistributor.PLAYER.with(player));
					this.subscribes.put(secondary, player);
				});
			} else if (primaryComponent == null) {
				BlockPos secondaryNode = secondaryComponent.getFirst();
				this.components.put(primary, secondaryComponent);
				secondaryComponent.add(primary);

				this.totalPower.add(secondaryNode, primaryPower);
				this.totalResistance.add(secondaryNode, primaryResistance);
				this.updateSpeed(secondaryNode);

				this.subscribes.removeAll(primary).forEach(player -> {
					ApiNetworkManager.INSTANCE.send(new RootSyncPacket(primary, secondaryNode),
							PacketDistributor.PLAYER.with(player));
					this.subscribes.put(secondaryNode, player);
				});
			} else if (secondaryComponent == null) {
				BlockPos primaryNode = primaryComponent.getFirst();
				this.components.put(secondary, primaryComponent);
				primaryComponent.add(secondary);

				this.totalPower.add(primaryNode, secondaryPower);
				this.totalResistance.add(primaryNode, secondaryResistance);
				this.updateSpeed(primaryNode);

				this.subscribes.removeAll(secondary).forEach(player -> {
					ApiNetworkManager.INSTANCE.send(new RootSyncPacket(secondary, primaryNode),
							PacketDistributor.PLAYER.with(player));
					this.subscribes.put(primaryNode, player);
				});
			} else if (primaryComponent != secondaryComponent) {
				BlockPos primaryNode = primaryComponent.getFirst();
				BlockPos secondaryNode = secondaryComponent.getFirst();
				secondaryComponent.forEach(pos -> { // TODO size
					primaryComponent.add(pos);
					this.components.put(pos, primaryComponent);
				});
				this.subscribes.removeAll(secondaryNode).forEach(player -> {
					ApiNetworkManager.INSTANCE.send(new RootsSyncPacket(secondaryComponent, primaryNode),
							PacketDistributor.PLAYER.with(player));
					ApiNetworkManager.INSTANCE.send(new SpeedSyncPacket(secondaryNode, 0.0F),
							PacketDistributor.PLAYER.with(player));
					this.subscribes.put(primaryNode, player);
				});

				this.totalPower.add(primaryNode, this.totalPower.setCount(secondaryNode, 0));
				this.totalResistance.add(primaryNode, this.totalResistance.setCount(secondaryNode, 0));

				this.updateSpeed(primaryNode);
				this.speeds.remove(secondaryNode);
			}
		}
	}

	private void serverTick() {
		for (Runnable runnable = this.tasks.poll(); runnable != null; runnable = this.tasks.poll()) {
			runnable.run();
		}
	}

	private void clientTick() {
		this.speeds.forEach((pos, speed) -> {
			RotateContext context = this.rotates.computeIfAbsent(pos, blockPos -> new RotateContext(0.0F, 0.0F));
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
			return !this.queue.isEmpty();
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
