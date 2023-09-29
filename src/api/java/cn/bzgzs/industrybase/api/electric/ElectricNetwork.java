package cn.bzgzs.industrybase.api.electric;

import cn.bzgzs.industrybase.api.CapabilityList;
import cn.bzgzs.industrybase.api.network.ApiNetworkManager;
import cn.bzgzs.industrybase.api.network.server.RemoveWiresPacket;
import cn.bzgzs.industrybase.api.network.server.WireConnSyncPacket;
import com.google.common.collect.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;

import java.util.*;

public class ElectricNetwork {
	private final Random random;
	private final HashMap<BlockPos, LinkedHashSet<BlockPos>> components;
	private final SetMultimap<BlockPos, Direction> sideConn;
	private final HashMultimap<BlockPos, BlockPos> wireConn;
	private final LevelAccessor level;
	private final ArrayDeque<Runnable> tasks;
	private final HashMap<BlockPos, Double> totalOutput;
	private final HashMap<BlockPos, Double> totalInput;
	private final HashMultiset<BlockPos> FEEnergy;
	private final HashMultiset<BlockPos> FEInput;
	private final HashMap<BlockPos, Double> machineOutput;
	private final HashMap<BlockPos, Double> machineInput;
	private final SetMultimap<BlockPos, Direction> FEMachines;
	private final HashMultimap<BlockPos, ServerPlayer> subscribes;

	public ElectricNetwork(LevelAccessor level) {
		this.random = new Random();
		this.components = new HashMap<>();
		this.sideConn = Multimaps.newSetMultimap(new HashMap<>(), () -> EnumSet.noneOf(Direction.class));
		this.wireConn = HashMultimap.create();
		this.level = level;
		this.tasks = new ArrayDeque<>();
		this.totalOutput = new HashMap<>();
		this.totalInput = new HashMap<>();
		this.FEEnergy = HashMultiset.create();
		this.FEInput = HashMultiset.create();
		this.machineOutput = new HashMap<>();
		this.machineInput = new HashMap<>();
		this.FEMachines = Multimaps.newSetMultimap(new HashMap<>(), () -> EnumSet.noneOf(Direction.class));
		this.subscribes = HashMultimap.create();
	}

	public int size(BlockPos pos) {
		return this.components.containsKey(pos) ? this.components.get(pos).size() : 1;
	}

	public BlockPos root(BlockPos pos) {
		return this.components.containsKey(pos) ? this.components.get(pos).iterator().next() : pos;
	}

	public double getTotalOutput(BlockPos pos) {
		return this.totalOutput(this.root(pos));
	}

	private double totalOutput(BlockPos root) {
		return this.totalOutput.getOrDefault(root, 0.0D);
	}

	public double getTotalInput(BlockPos pos) {
		return this.totalInput(this.root(pos));
	}

	private double totalInput(BlockPos root) {
		return this.totalInput.getOrDefault(root, 0.0D);
	}

	public Set<BlockPos> getWireConn(BlockPos pos) {
		return this.wireConn.get(pos);
	}

	public Set<BlockPos> subscribeWire(BlockPos pos, ServerPlayer player) {
		this.subscribes.put(pos, player);
		return this.wireConn.get(pos);
	}

	public void unsubscribeWire(BlockPos pos, ServerPlayer player) {
		this.subscribes.remove(pos, player);
	}

	public void addClientWire(BlockPos from, BlockPos to) {
		if (this.level.isClientSide()) {
			this.wireConn.put(from, to);
		}
	}

	public void addClientWire(BlockPos pos, Collection<BlockPos> data) {
		if (this.level.isClientSide()) {
			this.wireConn.putAll(pos, data);
		}
	}

	public void removeClientWire(BlockPos from, BlockPos to) {
		if (this.level.isClientSide()) {
			this.wireConn.remove(from, to);
		}
	}

	public void removeClientWires(BlockPos from) {
		if (this.level.isClientSide()) {
			this.wireConn.get(from).forEach(to -> this.wireConn.remove(to, from));
			this.wireConn.removeAll(from);
		}
	}

	public double getMachineOutput(BlockPos pos) {
		return this.machineOutput.getOrDefault(pos, 0.0D);
	}
	
	public double setMachineOutput(BlockPos pos, double power) {
		if (this.getMachineOutput(pos) == power) return 0;
		double diff;
		if (power > 0) {
			diff = power - this.getMachineOutput(pos);
			this.machineOutput.put(pos, power);
		} else {
			diff = -this.getMachineOutput(pos);
			this.machineOutput.remove(pos);
		}
		BlockPos root = this.root(pos);
		double output = this.totalOutput(root) + diff;
		if (output > 0) {
			this.totalOutput.put(root, output);
		} else {
			this.totalOutput.remove(root);
		}
		return diff;
	}

	public double getMachineInput(BlockPos pos) {
		return this.machineInput.getOrDefault(pos, 0.0D);
	}

	public double setMachineInput(BlockPos pos, double power) {
		if (this.getMachineInput(pos) == power) return 0;
		double diff;
		if (power > 0) {
			diff = power - this.getMachineInput(pos);
			this.machineInput.put(pos, power);
		} else {
			diff = -this.getMachineInput(pos);
			this.machineInput.remove(pos);
		}
		BlockPos root = this.root(pos);
		double input = this.totalInput(root) + diff;
		if (input > 0) {
			this.totalInput.put(root, input);
		} else {
			this.totalInput.remove(root);
		}
		return diff;
	}

	public double getRealInput(BlockPos pos) {
		BlockPos root = this.root(pos);
		double totalOutput = this.totalOutput(root) + this.FEInput.count(root);
		double totalInput = this.totalInput(root);
		double machineInput = this.getMachineInput(pos);
		if (totalInput > 0.0D) {
			if (totalOutput >= totalInput) {
				return this.getMachineInput(pos);
			} else {
				return machineInput * totalOutput / totalInput;
			}
		}
		return 0;
	}

	public int getFEEnergy(BlockPos pos) {
		return this.FEEnergy.count(this.root(pos));
	}

	public int getMaxFEStored(BlockPos pos) {
		return 100 * this.size(pos);
	}

	public int receiveFEEnergy(BlockPos pos, int maxReceive, boolean simulate) {
		BlockPos root = this.root(pos);
		int receive = Math.min(maxReceive, this.getMaxFEStored(root) - this.FEEnergy.count(root));
		if (!simulate) this.FEEnergy.add(root, receive);
		return receive;
	}

	public int extractFEEnergy(BlockPos pos, int maxExtract, boolean simulate) {
		BlockPos root = this.root(pos);
		int extract = Math.min(maxExtract, this.FEEnergy.count(root));
		if (!simulate) this.FEEnergy.remove(root, extract);
		return extract;
	}

	@SuppressWarnings("deprecation")
	public void removeBlock(BlockPos pos, Runnable callback) {
		this.tasks.offer(() -> {
			this.setMachineOutput(pos, 0);
			this.setMachineInput(pos, 0);
			for (Direction side : Direction.values()) {
				this.cutSide(pos, side);
			}
			this.FEMachines.removeAll(pos); // 移除相应的 FE 机器
			if (!this.wireConn.get(pos).isEmpty()) {
				Iterator<BlockPos> iterator = this.wireConn.get(pos).iterator();
				while (iterator.hasNext()) {
					BlockPos another = iterator.next();
					iterator.remove();
					this.wireConn.remove(another, pos);
					this.spilt(pos, another);
					if (this.level.isAreaLoaded(another, 0)) {
						this.level.getChunk(another).setUnsaved(true);
					}
				}
				this.subscribes.get(pos).forEach(player -> ApiNetworkManager.INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), new RemoveWiresPacket(pos)));
				this.subscribes.removeAll(pos);
			}
			callback.run();
		});
	}

	public void removeWire(BlockPos from, BlockPos to) {
		this.tasks.offer(() -> {
			this.cutWire(from, to);
			this.level.getChunk(from).setUnsaved(true); // 不能检查区块是否加载
			this.level.getChunk(to).setUnsaved(true);
		});
	}

	private void cutSide(BlockPos node, Direction direction) {
		if (this.sideConn.remove(node, direction)) {
			BlockPos another = node.relative(direction);
			this.sideConn.remove(another, direction.getOpposite());
			this.spilt(node, another);
		}
	}

	public void cutWire(BlockPos from, BlockPos to) {
		if (this.wireConn.remove(from, to)) {
			this.wireConn.remove(to, from);
			this.spilt(from, to);
			this.subscribes.get(from).forEach(player -> ApiNetworkManager.INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), new WireConnSyncPacket(from, to, true)));
			this.subscribes.get(to).forEach(player -> ApiNetworkManager.INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), new WireConnSyncPacket(to, from, true)));
		}
	}

	private void spilt(BlockPos node, BlockPos another) {
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

			double outputDiff = this.getMachineOutput(secondaryNode);
			double inputDiff = this.getMachineInput(secondaryNode);
			double primaryOutput = this.totalOutput(primaryNode) - outputDiff;
			double primaryInput = this.totalInput(primaryNode) - inputDiff;
			if (primaryOutput >= 0) {
				this.totalOutput.put(primaryNode, primaryOutput);
			} else {
				this.totalOutput.remove(primaryNode);
			}
			if (primaryInput >= 0) {
				this.totalInput.put(primaryNode, primaryInput);
			} else {
				this.totalInput.remove(primaryNode);
			}
		} else {
			double outputDiff = 0.0D;
			double inputDiff = 0.0D;
			for (BlockPos pos : secondaryComponent) {
				this.components.put(pos, secondaryComponent);

				outputDiff += this.getMachineOutput(pos);
				inputDiff += this.getMachineInput(pos);

			}
			double primaryOutput = this.totalOutput(primaryNode) - outputDiff;
			double primaryInput = this.totalInput(primaryNode) - inputDiff;
			if (primaryOutput >= 0) {
				this.totalOutput.put(primaryNode, primaryOutput);
			} else {
				this.totalOutput.remove(primaryNode);
			}
			if (primaryInput >= 0) {
				this.totalInput.put(primaryNode, primaryInput);
			} else {
				this.totalInput.remove(primaryNode);
			}
			if (outputDiff > 0) this.totalOutput.put(secondaryNode, outputDiff);
			if (inputDiff > 0) this.totalInput.put(secondaryNode, inputDiff);
		}
		if (primaryComponent.size() <= 1) {
			this.components.remove(primaryNode);

			if (this.totalOutput(primaryNode) <= 0) this.totalOutput.remove(primaryNode);
			if (this.totalInput(primaryNode) <= 0) this.totalInput.remove(primaryNode);
		}
		// 分配 FE 能量
		int primarySize = this.size(primaryNode), secondarySize = this.size(secondaryNode);
		int diff = this.FEEnergy.count(primaryNode) * secondarySize / (primarySize + secondarySize);
		this.FEEnergy.remove(primaryNode, diff);
		this.FEEnergy.add(secondaryNode, diff);
	}

	public void addOrChangeBlock(BlockPos pos, Runnable callback) {
		this.tasks.offer(() -> {
			for (Direction side : Direction.values()) {
				if (this.hasElectricalCapability(pos, side)) {
					if (this.hasElectricalCapability(pos.relative(side), side.getOpposite())) {
						this.FEMachines.remove(pos.immutable(), side);
						this.linkSide(pos, side);
					} else if (this.hasFECapability(pos.relative(side), side.getOpposite())) {
						this.FEMachines.put(pos.immutable(), side);
						this.cutSide(pos, side);
					} else {
						this.FEMachines.remove(pos.immutable(), side);
						this.cutSide(pos, side);
					}
				} else {
					this.FEMachines.remove(pos.immutable(), side);
					this.cutSide(pos, side);
				}
			}
			callback.run();
		});
	}

	public boolean addWire(BlockPos from, BlockPos to, Runnable callback) {
		if (!from.equals(to) && !this.wireConn.containsEntry(from, to)) {
			return this.tasks.offer(() -> {
				linkWire(from, to);
				callback.run(); // 只需要一方保存即可，不影响最终加载后的连通域
			});
		}
		return false;
	}

	@SuppressWarnings("deprecation")
	private boolean hasElectricalCapability(BlockPos pos, Direction side) {
		if (this.level.isAreaLoaded(pos, 0)) {
			BlockEntity blockEntity = this.level.getBlockEntity(pos);
			return blockEntity != null && blockEntity.getCapability(CapabilityList.ELECTRIC_POWER, side).isPresent();
		}
		return false;
	}

	@SuppressWarnings("deprecation")
	private boolean hasFECapability(BlockPos pos, Direction side) {
		if (this.level.isAreaLoaded(pos, 0)) {
			BlockEntity blockEntity = this.level.getBlockEntity(pos);
			return blockEntity != null && blockEntity.getCapability(ForgeCapabilities.ENERGY, side).isPresent();
		}
		return false;
	}

	private void linkSide(BlockPos node, Direction direction) {
		BlockPos secondary = node.immutable();
		if (this.sideConn.put(secondary, direction)) {
			BlockPos primary = secondary.relative(direction);
			this.sideConn.put(primary, direction.getOpposite());
			this.link(primary, secondary);
		}
	}

	private void linkWire(BlockPos from, BlockPos to) {
		BlockPos secondary = from.immutable();
		BlockPos primary = to.immutable();
		if (this.wireConn.put(secondary, primary)) {
			this.wireConn.put(primary, secondary);
			this.link(primary, secondary);
			this.subscribes.get(from).forEach(player -> ApiNetworkManager.INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), new WireConnSyncPacket(from, to, false)));
			this.subscribes.get(to).forEach(player -> ApiNetworkManager.INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), new WireConnSyncPacket(to, from, false)));
		}
	}

	private void link(BlockPos primary, BlockPos secondary) {
		LinkedHashSet<BlockPos> primaryComponent = this.components.get(primary);
		LinkedHashSet<BlockPos> secondaryComponent = this.components.get(secondary);

		double primaryOutput = this.getMachineOutput(primary);
		double secondaryOutput = this.getMachineOutput(secondary);
		double primaryInput = this.getMachineInput(primary);
		double secondaryInput = this.getMachineInput(secondary);

		if (primaryComponent == null && secondaryComponent == null) {
			LinkedHashSet<BlockPos> union = new LinkedHashSet<>();
			this.components.put(secondary, union);
			this.components.put(primary, union);
			union.add(secondary);
			union.add(primary);

			this.totalOutput.put(secondary, primaryOutput + secondaryOutput);
			this.totalInput.put(secondary, primaryInput + secondaryInput);
			this.mergeFE(secondary, primary);
		} else if (primaryComponent == null) {
			BlockPos secondaryNode = secondaryComponent.iterator().next();
			this.components.put(primary, secondaryComponent);
			secondaryComponent.add(primary);

			double outputOld = this.totalOutput(secondaryNode);
			this.totalOutput.put(secondaryNode, outputOld + primaryOutput);
			double inputOld = this.totalInput(secondaryNode);
			this.totalInput.put(secondaryNode, inputOld + primaryInput);
			this.mergeFE(secondaryNode, primary);
		} else if (secondaryComponent == null) {
			BlockPos primaryNode = primaryComponent.iterator().next();
			this.components.put(secondary, primaryComponent);
			primaryComponent.add(secondary);

			double outputOld = this.totalOutput(primaryNode);
			this.totalOutput.put(primaryNode, outputOld + secondaryOutput);
			double inputOld = this.totalInput(primaryNode);
			this.totalInput.put(primaryNode, inputOld + secondaryInput);
			this.mergeFE(primaryNode, secondary);
		} else if (primaryComponent != secondaryComponent) {
			BlockPos primaryNode = primaryComponent.iterator().next();
			BlockPos secondaryNode = secondaryComponent.iterator().next();
			LinkedHashSet<BlockPos> union = new LinkedHashSet<>(Sets.union(primaryComponent, secondaryComponent));
			union.forEach(pos -> this.components.put(pos, union));

			double outputDiff = this.totalOutput(secondaryNode);
			double outputOld = this.totalOutput(primaryNode);
			this.totalOutput.put(primaryNode, outputOld + outputDiff);
			this.totalOutput.remove(secondaryNode);

			double inputDiff = this.totalInput(secondaryNode);
			double inputOld = this.totalInput(primaryNode);
			this.totalInput.put(primaryNode, inputOld + inputDiff);
			this.totalInput.remove(secondaryNode);
			this.mergeFE(primaryNode, secondaryNode);
		}
	}

	private void mergeFE(BlockPos primaryNode, BlockPos secondaryNode) {
		int diff = this.FEEnergy.count(secondaryNode);
		this.FEEnergy.remove(secondaryNode, diff);
		this.FEEnergy.add(primaryNode, diff);
	}

	private void tickStart() {
		for (Runnable runnable = this.tasks.poll(); runnable != null; runnable = this.tasks.poll()) {
			runnable.run();
		}
	}

	@SuppressWarnings("deprecation")
	private void tickEnd() {
		HashSet<BlockPos> updated = new HashSet<>();
		Multiset<BlockPos> forgeEnergy = HashMultiset.create();
		for (Map.Entry<BlockPos, Direction> entry : this.shuffle(this.FEMachines.entries())) {
			BlockPos pos = entry.getKey();
			Direction direction = entry.getValue();
			BlockPos target = pos.relative(direction);
			if (this.level.isAreaLoaded(target, 0)) {
				BlockPos root = this.root(pos);
				// 将剩余 EP 转换为 FE
				if (updated.add(root)) { // 已转换过的能量网络则跳过
					double power = this.totalOutput(root);
					double energy = power - this.totalInput(root);
					if (energy > 0.0D) {
						forgeEnergy.add(root, (int) Math.floor(energy));
					}
				}

				// 向 FE 方块输出能量
				Optional.ofNullable(this.level.getBlockEntity(target)).ifPresent(blockEntity -> blockEntity.getCapability(ForgeCapabilities.ENERGY, direction.getOpposite()).ifPresent(capability -> {
					if (capability.canReceive()) {
						int diff = forgeEnergy.count(root);
						int FEDiff = this.FEEnergy.count(root);
						forgeEnergy.remove(root, capability.receiveEnergy(diff, false));
						if (forgeEnergy.count(root) <= 0) { // 先将 EP 转化的 FE 分配完
							this.FEEnergy.remove(root, capability.receiveEnergy(FEDiff, false));
						}
					}
				}));
			}
		}
		// 将未使用 FE 转化为 EP
		this.FEInput.clear(); // 清除先前转换的 EP
		this.FEEnergy.forEachEntry((root, count) -> {
			double lack = this.totalInput(root) - this.totalOutput(root);
			if (lack > 0) {
				this.FEInput.setCount(root, Math.min((int) Math.ceil(lack), count));
			}
		});
		this.FEInput.forEachEntry(this.FEEnergy::remove);
	}

	private <T> List<T> shuffle(Collection<? extends T> iterable) {
		List<T> list = new ArrayList<>(iterable);
		Collections.shuffle(list, this.random);
		return list;
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
			return this.queue.size() > 0;
		}

		@Override
		public BlockPos next() {
			BlockPos node = this.queue.remove();
			for (Direction direction : ElectricNetwork.this.sideConn.get(node)) {
				BlockPos another = node.relative(direction);
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

		public LinkedHashSet<BlockPos> getSearched() {
			return this.searched;
		}
	}

	@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE)
	public static class Manager {
		private static final Map<LevelAccessor, ElectricNetwork> INSTANCES = new IdentityHashMap<>();

		public static ElectricNetwork get(LevelAccessor level) {
			return INSTANCES.computeIfAbsent(Objects.requireNonNull(level, "Level can't be null!"), ElectricNetwork::new);
		}

		@SubscribeEvent
		public static void onUnload(LevelEvent.Unload event) {
			INSTANCES.remove(event.getLevel());
		}

		@SubscribeEvent
		public static void onLevelTick(TickEvent.LevelTickEvent event) {
			if (event.side.isServer()) {
				ElectricNetwork network = get(event.level);
				if (event.phase == TickEvent.Phase.START) {
					network.tickStart();
				} else {
					network.tickEnd();
				}
			}
		}
	}
}
