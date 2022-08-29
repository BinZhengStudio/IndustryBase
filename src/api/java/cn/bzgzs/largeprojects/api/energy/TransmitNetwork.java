package cn.bzgzs.largeprojects.api.energy;

import cn.bzgzs.largeprojects.api.CapabilityList;
import cn.bzgzs.largeprojects.api.IMachine;
import cn.bzgzs.largeprojects.api.util.BlockConnectNetwork;
import com.google.common.collect.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.common.Mod;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class TransmitNetwork {
	private final LevelAccessor level;
	private final BlockConnectNetwork network;
	private final Queue<Runnable> tasks;
	private final Set<BlockPos> updatePowerNodes;
	private final Set<BlockPos> updateResistanceNodes;
	private final Set<BlockPos> updateSpeedNetworks;
	private final Multiset<BlockPos> powerCollection; // BlockPos是中心块的坐标，出现个数为连通域总功率的数值
	private final Multiset<BlockPos> resistanceCollection;
	private final Map<BlockPos, Double> speedCollection;
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
		this.updatePowerNodes = new HashSet<>();
		this.updateResistanceNodes = new HashSet<>();
		this.updateSpeedNetworks = new HashSet<>();
		this.powerCollection = HashMultiset.create();
		this.resistanceCollection = HashMultiset.create();
		this.speedCollection = new HashMap<>();
		this.chunks = Multimaps.newSetMultimap(new HashMap<>(), HashSet::new);
		this.machineMap = Multimaps.newSetMultimap(new HashMap<>(), HashSet::new);
	}

	public int size(BlockPos pos) {
		return this.network.size(pos);
	}

	public int totalPower(BlockPos pos) {
		BlockPos root = this.network.root(pos);
		return this.powerCollection.count(root);
	}

	public int totalResistance(BlockPos pos) {
		BlockPos root = this.network.root(pos);
		return this.resistanceCollection.count(root);
	}

	public double speed(BlockPos pos) {
		BlockPos root = this.network.root(pos);
		return this.speedCollection.get(root);
	}

	public void removeBlock(BlockPos pos, Runnable callback) {
		this.tasks.offer(() -> {
			this.chunks.remove(new ChunkPos(pos), pos);
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
	}

	public void addOrChangeBlock(BlockPos pos, Runnable callback) {
		this.tasks.offer(() -> {
			this.chunks.put(new ChunkPos(pos), pos.immutable());
			for (Direction side : Direction.values()) {
				if (this.hasMechanicalConnection(pos, side)) { // 某个方向上有与其他传动设备连接
					this.network.link(pos, side, this::beforeMerge);
				} else {
					this.network.cut(pos, side, this::afterSplit);
				}
			}
			if (this.level.getBlockState(pos).getBlock() instanceof IMachine) { // 如果是机器则添加到对应map中
				this.machineMap.put(this.network.root(pos), pos.immutable());
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

	private void beforeMerge(BlockPos primaryNode, BlockPos secondaryNode) { // TODO 不适用（并能量）
		this.markPowerChanged(primaryNode);
		this.markResistanceChanged(primaryNode);
	}

	private void tickStart() {
		for (Runnable runnable = this.tasks.poll(); runnable != null; runnable = this.tasks.poll()) {
			runnable.run();
		}
		this.updateSpeedNetworks.clear();
		this.updateSpeedNetworks.addAll(this.updatePower());
		this.updateSpeedNetworks.addAll(this.updateResistance());
	}

	private void tickEnd() {
		this.updateSpeedNetworks.forEach(root -> this.speedCollection.put(root, (double) this.powerCollection.count(root) / this.resistanceCollection.count(root)));
		this.updateSpeedNetworks.clear();
	}

	public boolean markPowerChanged(BlockPos pos) {
		return this.updatePowerNodes.add(pos.immutable());
	}

	@SuppressWarnings("deprecation")
	public Set<BlockPos> updatePower() {
		Set<BlockPos> updated = new HashSet<>();
		this.updatePowerNodes.forEach(pos -> {
			AtomicInteger power = new AtomicInteger();
			BlockPos root = this.network.root(pos);
			if (!updated.contains(root)) {
				this.machineMap.get(root).forEach(machinePos -> {
					if (this.level.isAreaLoaded(machinePos, 0)) {
						Optional.ofNullable(this.level.getBlockEntity(machinePos)).ifPresent(blockEntity -> blockEntity.getCapability(CapabilityList.MECHANICAL_TRANSMIT, this.network.getConnections().get(machinePos).iterator().next()).ifPresent(transmit -> power.addAndGet(transmit.getPower())));
					}
				});
				updated.add(root);
			}
		});
		this.updatePowerNodes.clear();
		return updated;
	}

	public boolean markResistanceChanged(BlockPos pos) {
		return this.updateResistanceNodes.add(pos.immutable());
	}

	@SuppressWarnings("deprecation")
	public Set<BlockPos> updateResistance() {
		Set<BlockPos> updated = new HashSet<>();
		this.updateResistanceNodes.forEach(pos -> {
			AtomicInteger resistance = new AtomicInteger();
			BlockPos root = this.network.root(pos);
			if (!updated.contains(root)) {
				this.machineMap.get(root).forEach(machinePos -> {
					if (this.level.isAreaLoaded(machinePos, 0)) {
						Optional.ofNullable(this.level.getBlockEntity(machinePos)).ifPresent(blockEntity -> blockEntity.getCapability(CapabilityList.MECHANICAL_TRANSMIT, this.network.getConnections().get(machinePos).iterator().next()).ifPresent(transmit -> resistance.addAndGet(transmit.getResistance())));
					}
				});
				updated.add(root);
			}
		});
		this.updateResistanceNodes.clear();
		return updated;
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
			if (LogicalSide.SERVER.equals(event.side)) {
				switch (event.phase) {
					case START -> get(event.level).tickStart();
					case END -> get(event.level).tickEnd();
				}
			}
		}
	}
}
