package cn.bzgzs.largeprojects.api.energy;

import cn.bzgzs.largeprojects.api.CapabilityList;
import cn.bzgzs.largeprojects.api.IMachine;
import cn.bzgzs.largeprojects.api.event.TransmitNetworkEvent;
import cn.bzgzs.largeprojects.api.util.BlockConnectNetwork;
import com.google.common.collect.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class TransmitNetwork {
	private final LevelAccessor level;
	private final BlockConnectNetwork network;
	private final Queue<Runnable> tasks;
	private final Set<BlockPos> updatePowerNetworks;
	private final Set<BlockPos> updateResistanceNetworks;
	private final Set<BlockPos> updateRootNetworks;
	private final Multiset<BlockPos> powerCollection; // BlockPos是中心块的坐标，出现个数为连通域总功率的数值
	private final Multiset<BlockPos> resistanceCollection;
	private final Map<BlockPos, Double> speedCollection;
	private final Map<BlockPos, BlockPos> rootCollection;
	private final SetMultimap<ChunkPos, BlockPos> chunks;
	/**
	 * 机器们。
	 * 前一个BlockPos是root，后面是连通域中机器坐标。
	 * <p>Machines.
	 * The first BlockPos is the position of root, the second is the position of the machine.
	 */
	private final SetMultimap<BlockPos, BlockPos> machineMap;

	public TransmitNetwork(LevelAccessor level, BlockConnectNetwork network) {
		this.level = level;
		this.network = network;
		this.tasks = Queues.newArrayDeque();
		this.updatePowerNetworks = new HashSet<>();
		this.updateResistanceNetworks = new HashSet<>();
		this.updateRootNetworks = new HashSet<>();
		this.powerCollection = HashMultiset.create();
		this.resistanceCollection = HashMultiset.create();
		this.speedCollection = new HashMap<>();
		this.rootCollection = new HashMap<>();
		this.chunks = Multimaps.newSetMultimap(new HashMap<>(), HashSet::new);
		this.machineMap = Multimaps.newSetMultimap(new HashMap<>(), HashSet::new);
	}

	public int size(BlockPos pos) {
		return this.network.size(pos);
	}

	public BlockPos root(BlockPos pos) {
		return this.level.isClientSide() ? this.rootCollection.getOrDefault(pos, pos) : this.network.root(pos);
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
		Double speed = this.speedCollection.get(root);
		return speed != null ? speed : 0.0D;
	}

	public void removeBlock(BlockPos pos, Runnable callback) {
		this.tasks.offer(() -> {
			this.chunks.remove(new ChunkPos(pos), pos);
			this.speedCollection.remove(pos);
			MinecraftForge.EVENT_BUS.post(new TransmitNetworkEvent.UpdateSpeedEvent(this.level, new HashMap<>(), Set.of(pos)));
			this.rootCollection.remove(pos);
			MinecraftForge.EVENT_BUS.post(new TransmitNetworkEvent.UpdateRootEvent(this.level, new HashMap<>(), Set.of(pos)));
			this.machineMap.remove(this.network.root(pos), pos);
			for (Direction side : Direction.values()) {
				this.network.cut(pos, side, this::afterSplit);
			}
			callback.run();
		});
	}

	private void afterSplit(BlockPos primaryNode, BlockPos secondaryNode) {
		this.markPowerChanged(primaryNode);
		this.markPowerChanged(secondaryNode);
		this.markResistanceChanged(primaryNode);
		this.markResistanceChanged(secondaryNode);

		/*
		network会将只有单个方块的连通域删掉以节省内存和时间
		因此需要将被删掉的单方块连通域中的机械删掉
		 */
		if (!this.network.hasComponent(secondaryNode)) { // TODO 性能可以优化
			this.machineMap.remove(primaryNode, secondaryNode);
			this.rootCollection.remove(secondaryNode);
			MinecraftForge.EVENT_BUS.post(new TransmitNetworkEvent.UpdateRootEvent(this.level, new HashMap<>(), Set.of(secondaryNode)));
		} else {
			this.machineMap.get(primaryNode).forEach(pos -> {
				if (this.network.root(pos).equals(secondaryNode)) {
					this.machineMap.put(secondaryNode, pos);
					this.machineMap.remove(primaryNode, pos);
				}
			});

			Map<BlockPos, BlockPos> updatedData = new HashMap<>();
			this.network.getComponents(secondaryNode).forEach(pos -> {
				this.rootCollection.put(pos, secondaryNode);
				updatedData.put(pos, secondaryNode);
			});
			MinecraftForge.EVENT_BUS.post(new TransmitNetworkEvent.UpdateRootEvent(this.level, updatedData, new HashSet<>()));
		}
	}

	public void addOrChangeBlock(BlockPos pos, Runnable callback) {
		this.tasks.offer(() -> {
			this.chunks.put(new ChunkPos(pos), pos.immutable());
			for (Direction side : Direction.values()) {
				if (this.hasMechanicalConnection(pos, side)) { // 某个方向上有与其他传动设备连接
					this.network.link(pos, side, this::beforeMerge);
					if (this.level.getBlockState(pos).getBlock() instanceof IMachine) { // 如果是机器则添加到对应map中
						this.machineMap.put(this.network.root(pos), pos.immutable());
					}
					if (this.level.getBlockState(pos.offset(side.getNormal())).getBlock() instanceof IMachine) {
						this.machineMap.put(this.network.root(pos), pos.immutable());
					}
				} else {
					this.network.cut(pos, side, this::afterSplit);
				}
			}
			this.rootCollection.put(pos, this.network.root(pos));
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

	private void beforeMerge(BlockPos primaryNode, BlockPos secondaryNode) {
		this.markPowerChanged(primaryNode); // TODO 可通过增量的方式优化性能
		this.markResistanceChanged(primaryNode);

		this.machineMap.putAll(primaryNode, this.machineMap.get(secondaryNode));
		this.machineMap.removeAll(secondaryNode);

		Map<BlockPos, BlockPos> updatedData = new HashMap<>();
		this.network.getComponents(secondaryNode).forEach(pos -> {
			this.rootCollection.put(pos, primaryNode);
			updatedData.put(pos, primaryNode);
		});
		MinecraftForge.EVENT_BUS.post(new TransmitNetworkEvent.UpdateRootEvent(this.level, updatedData, new HashSet<>()));
	}

	private void tickStart() {
		for (Runnable runnable = this.tasks.poll(); runnable != null; runnable = this.tasks.poll()) {
			runnable.run();
		}
	}

	private void tickEnd() { // TODO 在连通域很大时有极大性能问题
		this.updateSpeedCollection();
		this.updateRootCollection();
	}

	public boolean markPowerChanged(BlockPos pos) {
		return this.updatePowerNetworks.add(this.network.root(pos));
	}

	@SuppressWarnings("deprecation")
	private Multiset<BlockPos> updatePower() {
		Multiset<BlockPos> updatedData = HashMultiset.create();
		if (!this.updatePowerNetworks.isEmpty()) {
			Set<BlockPos> deleted = new HashSet<>();
			this.updatePowerNetworks.forEach(root -> {
				AtomicInteger power = new AtomicInteger();
				this.machineMap.get(root).forEach(machinePos -> {
					if (this.level.isAreaLoaded(machinePos, 0)) {
						Optional.ofNullable(this.level.getBlockEntity(machinePos)).ifPresent(blockEntity -> { // TODO side
							blockEntity.getCapability(CapabilityList.MECHANICAL_TRANSMIT, this.network.getConnections(root).iterator().next()).ifPresent(transmit -> power.addAndGet(transmit.getPower()));
						});
					}
				});
				this.powerCollection.setCount(root, power.get());
				if (power.get() > 0) {
					updatedData.setCount(root, power.get());
				} else {
					deleted.add(root);
				}
			});
			this.updatePowerNetworks.clear();
			MinecraftForge.EVENT_BUS.post(new TransmitNetworkEvent.UpdatePowerEvent(this.level, updatedData, deleted));
		}
		return updatedData;
	}

	public boolean markResistanceChanged(BlockPos pos) {
		return this.updateResistanceNetworks.add(this.network.root(pos));
	}

	@SuppressWarnings("deprecation")
	private Multiset<BlockPos> updateResistance() {
		Multiset<BlockPos> updatedData = HashMultiset.create();
		if (!this.updateResistanceNetworks.isEmpty()) {
			Set<BlockPos> deleted = new HashSet<>();
			this.updateResistanceNetworks.forEach(root -> {
				AtomicInteger resistance = new AtomicInteger();
				this.machineMap.get(root).forEach(machinePos -> {
					if (this.level.isAreaLoaded(machinePos, 0)) {
						Optional.ofNullable(this.level.getBlockEntity(machinePos)).ifPresent(blockEntity -> { // TODO side
							blockEntity.getCapability(CapabilityList.MECHANICAL_TRANSMIT, this.network.getConnections(root).iterator().next()).ifPresent(transmit -> resistance.addAndGet(transmit.getResistance()));
						});
					}
				});
				this.resistanceCollection.setCount(root, resistance.get());
				if (resistance.get() > 0) {
					updatedData.setCount(root, resistance.get());
				} else {
					deleted.add(root);
				}
			});
			this.updateResistanceNetworks.clear();
			MinecraftForge.EVENT_BUS.post(new TransmitNetworkEvent.UpdateResistanceEvent(this.level, updatedData, deleted));
		}
		return updatedData;
	}

	private void updateSpeedCollection() {
		Set<BlockPos> updateSpeedNetworks = new HashSet<>();
		updateSpeedNetworks.addAll(this.updatePower());
		updateSpeedNetworks.addAll(this.updateResistance());
		if (!updateSpeedNetworks.isEmpty()) {
			Map<BlockPos, Double> updatedData = new HashMap<>();
			Set<BlockPos> deleted = new HashSet<>();
			updateSpeedNetworks.forEach(root -> {
				if (this.powerCollection.count(root) > 0 && this.resistanceCollection.count(root) > 0) {
					double speed = (double) this.powerCollection.count(root) / this.resistanceCollection.count(root);
					this.speedCollection.put(root, speed);
					updatedData.put(root, speed);
				} else {
					this.speedCollection.remove(root);
					deleted.add(root);
				}
			});
			MinecraftForge.EVENT_BUS.post(new TransmitNetworkEvent.UpdateSpeedEvent(this.level, updatedData, deleted));
		}
	}

	private boolean markRootChanged(BlockPos pos) {
		return this.updateRootNetworks.add(this.network.root(pos));
	}

	private void updateRootCollection() {
		if (!this.updateRootNetworks.isEmpty()) {
			Map<BlockPos, BlockPos> updatedData = new HashMap<>();
			this.updateRootNetworks.forEach(root -> this.network.getComponents(root).forEach(pos -> {
				this.rootCollection.put(pos, root);
				updatedData.put(pos, root);
			}));
			MinecraftForge.EVENT_BUS.post(new TransmitNetworkEvent.UpdateRootEvent(this.level, updatedData, new HashSet<>()));
			this.updateRootNetworks.clear();
		}
	}

	@SuppressWarnings("unused")
	public Multiset<BlockPos> getPowerCollection() {
		return this.powerCollection;
	}

	@SuppressWarnings("unused")
	public Multiset<BlockPos> getResistanceCollection() {
		return this.resistanceCollection;
	}

	public Map<BlockPos, Double> getSpeedCollection() {
		return this.speedCollection;
	}

	public Map<BlockPos, BlockPos> getRootCollection() {
		return this.rootCollection;
	}

	@SuppressWarnings("deprecation")
	private void markDirty() { // TODO 可能由各BlockEntity接管
		for (ChunkPos chunkPos : this.chunks.keys()) {
			BlockPos pos = chunkPos.getWorldPosition();
			if (this.level.isAreaLoaded(pos, 0)) {
				this.level.getChunk(pos).setUnsaved(true);
			}
		}
	}

	@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE)
	public static class Factory {
		private static final Map<LevelAccessor, TransmitNetwork> INSTANCES = Maps.newIdentityHashMap();

		public static TransmitNetwork get(LevelAccessor level) {
			return INSTANCES.computeIfAbsent(level, k -> new TransmitNetwork(k, new BlockConnectNetwork()));
		}

		@SubscribeEvent
		public static void onSave(LevelEvent.Save event) { // TODO 可能由各BlockEntity接管
			if (INSTANCES.containsKey(event.getLevel())) {
				INSTANCES.get(event.getLevel()).markDirty();
			}
		}

		@SubscribeEvent
		public static void onUnload(LevelEvent.Unload event) {
			INSTANCES.remove(event.getLevel());
		}

		@SubscribeEvent
		public static void onWorldTick(TickEvent.LevelTickEvent event) {
			if (event.side.isServer()) {
				switch (event.phase) {
					case START -> get(event.level).tickStart();
					case END -> get(event.level).tickEnd();
				}
			}
		}
	}
}
