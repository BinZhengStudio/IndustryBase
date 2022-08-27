package cn.bzgzs.largeprojects.api.energy;

import cn.bzgzs.largeprojects.api.CapabilityList;
import cn.bzgzs.largeprojects.api.util.BlockConnectNetwork;
import com.google.common.collect.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class TransmitNetwork {
	private final Level level;
	private final BlockConnectNetwork network;
	private final Queue<Runnable> tasks;
	private final Set<BlockPos> updatePower;
	private final Multiset<BlockPos> powerCollection; // BlockPos是中心块的坐标，出现个数为连通域总功率的数值
	private final Multiset<BlockPos> resistanceCollection;
	private final SetMultimap<ChunkPos, BlockPos> chunks;
	private final SetMultimap<BlockPos, BlockPos> machineMap; // 前一个BlockPos是root，后面是连通域中机器坐标

	public TransmitNetwork(Level level, BlockConnectNetwork network) {
		this.level = level;
		this.network = network;
		this.tasks = Queues.newArrayDeque();
		this.updatePower = new HashSet<>();
		this.powerCollection = HashMultiset.create();
		this.resistanceCollection = HashMultiset.create();
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
		return (double) this.powerCollection.count(root) / this.resistanceCollection.count(root);
	}

	public void removeBlock(BlockPos pos, Runnable callback) {
		this.tasks.offer(() -> {
			this.chunks.remove(new ChunkPos(pos), pos);
			for (Direction side : Direction.values()) {
				this.network.cut(pos, side, this::afterSplit);
			}
			this.machineMap.removeAll(pos);
			callback.run();
		});
	}

	private void afterSplit(BlockPos primaryNode, BlockPos secondaryNode) { // TODO 不适用（分能量）
		int primarySize = this.network.size(primaryNode);
		int secondarySize = this.network.size(secondaryNode);
		int diff = this.powerCollection.count(primaryNode) * secondarySize / (primarySize + secondarySize);
		this.powerCollection.remove(primaryNode, diff);
		this.powerCollection.add(secondaryNode, diff);
	}

//	public void addBlock(BlockPos pos, Runnable callback) { // TODO 针对机器的呢？
//		this.tasks.offer(() -> {
//			this.chunks.put(new ChunkPos(pos), pos);
//			for (Direction side : Direction.values()) {
//				if (this.hasWireConnection(pos, side)) {
//					if (this.hasWireConnection(pos.offset(side.getNormal()), side.getOpposite())) {
//						this.machineCollection.remove(pos, side);
//						this.network.link(pos, side, this::beforeMerge);
//					} else {
//						this.machineCollection.put(pos, side);
//						this.network.cut(pos, side, this::afterSplit);
//					}
//				} else {
//					this.machineCollection.remove(pos, side);
//					this.network.cut(pos, side, this::afterSplit);
//				}
//			}
//			callback.run();
//		});
//	}

//	private boolean hasWireConnection(BlockPos pos, Direction side) {
//		if (this.level.isLoaded(pos)) {
//			BlockState state = this.level.getBlockState(pos);
//			return state.getBlock() == FEDemoWireBlock.BLOCK && state.get(FEDemoWireBlock.PROPERTY_MAP.get(side));
//		}
//		return false;
//	}

//	private void beforeMerge(BlockPos primaryNode, BlockPos secondaryNode) { // TODO 不适用（并能量）
//		int diff = this.energyCollection.count(secondaryNode);
//		this.energyCollection.remove(secondaryNode, diff);
//		this.energyCollection.add(primaryNode, diff);
//	}

	public void updatePower() {
		Set<BlockPos> updated = new HashSet<>();
		this.updatePower.forEach(pos -> {
			AtomicInteger power = new AtomicInteger();
			BlockPos root = this.network.root(pos);
			if (!updated.contains(root)) {
				Set<BlockPos> machines = this.machineMap.get(root);
				machines.forEach(machinePos -> Optional.ofNullable(this.level.getBlockEntity(machinePos)).ifPresentOrElse(blockEntity -> blockEntity.getCapability(CapabilityList.MECHANICAL_TRANSMIT, this.network.getConnections().get(machinePos).stream().toList().get(0)).ifPresent(transmit -> power.addAndGet(transmit.getPower())), () -> machines.remove(machinePos)));
				updated.add(root);
			}
		});
	}
}
