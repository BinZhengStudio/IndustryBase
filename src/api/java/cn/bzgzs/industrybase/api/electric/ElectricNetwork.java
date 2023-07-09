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
	private final Map<BlockPos, Set<BlockPos>> components;
	private final SetMultimap<BlockPos, Direction> sideConn;
	private final SetMultimap<BlockPos, BlockPos> wireConn;
	private final LevelAccessor level;
	private final Queue<Runnable> tasks;
	private final Map<BlockPos, Double> outputCollection;
	private final Map<BlockPos, Double> inputCollection;
	private final Map<BlockPos, Double> machineOutput;
	private final Map<BlockPos, Double> machineInput;
	private final SetMultimap<BlockPos, Direction> FEMachines;
	private final SetMultimap<BlockPos, ServerPlayer> subscribedWire;

	public ElectricNetwork(LevelAccessor level) {
		this.random = new Random();
		this.components = new HashMap<>();
		this.sideConn = Multimaps.newSetMultimap(new HashMap<>(), () -> EnumSet.noneOf(Direction.class));
		this.wireConn = HashMultimap.create();
		this.level = level;
		this.tasks = Queues.newArrayDeque();
		this.outputCollection = new HashMap<>();
		this.inputCollection = new HashMap<>();
		this.machineOutput = new HashMap<>();
		this.machineInput = new HashMap<>();
		this.FEMachines = Multimaps.newSetMultimap(new HashMap<>(), () -> EnumSet.noneOf(Direction.class));
		this.subscribedWire = HashMultimap.create();
	}

	public int size(BlockPos pos) {
		return this.components.containsKey(pos) ? this.components.get(pos).size() : 1;
	}

	public BlockPos root(BlockPos pos) {
		return this.components.containsKey(pos) ? this.components.get(pos).iterator().next() : pos;
	}

	public ImmutableSet<BlockPos> wireConnects(BlockPos pos) {
		return ImmutableSet.copyOf(this.wireConn.get(pos));
	}

	public double totalOutput(BlockPos pos) {
		BlockPos root = this.root(pos);
		return this.outputCollection.getOrDefault(root, 0.0D);
	}

	public double totalInput(BlockPos pos) {
		BlockPos root = this.root(pos);
		return this.inputCollection.getOrDefault(root, 0.0D);
	}

	public Set<BlockPos> getWireConn(BlockPos pos) {
		return this.wireConn.get(pos);
	}

	public Set<BlockPos> subscribeWire(BlockPos pos, ServerPlayer player) {
		this.subscribedWire.put(pos, player);
		return this.wireConn.get(pos);
	}

	public void unsubscribeWire(BlockPos pos, ServerPlayer player) {
		this.subscribedWire.remove(pos, player);
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
		double diff;
		if (power > 0) {
			diff = power - this.machineOutput.getOrDefault(pos, 0.0D);
			this.machineOutput.put(pos, power);
		} else {
			diff = -this.machineOutput.getOrDefault(pos, 0.0D);
			this.machineOutput.remove(pos);
		}
		BlockPos root = this.root(pos);
		double output = this.outputCollection.getOrDefault(root, 0.0D) + diff;
		if (output > 0) {
			this.outputCollection.put(root, output);
		} else {
			this.outputCollection.remove(root);
		}
		return diff;
	}

	public double getMachineInput(BlockPos pos) {
		return this.machineInput.getOrDefault(pos, 0.0D);
	}

	public double setMachineInput(BlockPos pos, double power) {
		double diff;
		if (power > 0) {
			diff = power - this.machineInput.getOrDefault(pos, 0.0D);
			this.machineInput.put(pos, power);
		} else {
			diff = -this.machineInput.getOrDefault(pos, 0.0D);
			this.machineInput.remove(pos);
		}
		BlockPos root = this.root(pos);
		double input = this.inputCollection.getOrDefault(root, 0.0D) + diff;
		if (input > 0) {
			this.inputCollection.put(root, input);
		} else {
			this.inputCollection.remove(root);
		}
		return diff;
	}

	public double getRealInput(BlockPos pos) {
		BlockPos root = this.root(pos);
		double totalOutput = this.outputCollection.getOrDefault(root, 0.0D);
		double totalInput = this.inputCollection.getOrDefault(root, 0.0D);
		double machineInput = this.machineInput.getOrDefault(pos, 0.0D);
		if (totalInput > 0.0D) {
			if (totalOutput >= totalInput) {
				return this.machineInput.getOrDefault(pos, 0.0D);
			} else {
				return machineInput * totalOutput / totalInput;
			}
		}
		return 0;
	}

	public void removeBlock(BlockPos pos, Runnable callback) {
		this.tasks.offer(() -> {
			this.setMachineOutput(pos, 0);
			this.setMachineInput(pos, 0);
			for (Direction side : Direction.values()) {
				this.cutSide(pos, side);
			}
			this.FEMachines.removeAll(pos); // 移除相应的 FE 机器
			if (!this.wireConn.get(pos).isEmpty()) {
				SetMultimap<BlockPos, BlockPos> multimap = HashMultimap.create();
				for (BlockPos another : new HashSet<>(this.wireConn.get(pos))) { // 需要在迭代时修改，所以这里要复制一下集合
					if (this.wireConn.remove(pos, another)) {
						this.wireConn.remove(another, pos);
						this.spilt(pos, another);
						multimap.put(pos, another);
						multimap.put(another, pos);
					}
					this.level.getChunk(another).setUnsaved(true); // 此处不能检查区块是否加载，无论连接的方块是否位于加载区块，都要强制保存
				}
				this.subscribedWire.get(pos).forEach(player -> ApiNetworkManager.INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), new RemoveWiresPacket(pos)));
				this.subscribedWire.removeAll(pos);
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
			this.subscribedWire.get(from).forEach(player -> ApiNetworkManager.INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), new WireConnSyncPacket(from, to, true)));
			this.subscribedWire.get(to).forEach(player -> ApiNetworkManager.INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), new WireConnSyncPacket(to, from, true)));
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
			double primaryOutput = this.outputCollection.getOrDefault(primaryNode, 0.0D) - outputDiff;
			double primaryInput = this.inputCollection.getOrDefault(primaryNode, 0.0D) - inputDiff;
			if (primaryOutput >= 0) {
				this.outputCollection.put(primaryNode, primaryOutput);
			} else {
				this.outputCollection.remove(primaryNode);
			}
			if (primaryInput >= 0) {
				this.inputCollection.put(primaryNode, primaryInput);
			} else {
				this.inputCollection.remove(primaryNode);
			}
		} else {
			double outputDiff = 0.0D;
			double inputDiff = 0.0D;
			for (BlockPos pos : secondaryComponent) {
				this.components.put(pos, secondaryComponent);

				outputDiff += this.machineOutput.getOrDefault(pos, 0.0D);
				inputDiff += this.machineInput.getOrDefault(pos, 0.0D);

			}
			double primaryOutput = this.outputCollection.getOrDefault(primaryNode, 0.0D) - outputDiff;
			double primaryInput = this.inputCollection.getOrDefault(primaryNode, 0.0D) - inputDiff;
			if (primaryOutput >= 0) {
				this.outputCollection.put(primaryNode, primaryOutput);
			} else {
				this.outputCollection.remove(primaryNode);
			}
			if (primaryInput >= 0) {
				this.inputCollection.put(primaryNode, primaryInput);
			} else {
				this.inputCollection.remove(primaryNode);
			}
			if (outputDiff > 0) this.outputCollection.put(secondaryNode, outputDiff);
			if (inputDiff > 0) this.inputCollection.put(secondaryNode, inputDiff);
		}
		if (primaryComponent.size() <= 1) {
			this.components.remove(primaryNode);

			if (this.outputCollection.getOrDefault(primaryNode, 0.0D) <= 0) this.outputCollection.remove(primaryNode);
			if (this.inputCollection.getOrDefault(primaryNode, 0.0D) <= 0) this.inputCollection.remove(primaryNode);
		}
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

	private void linkWire(BlockPos from, BlockPos to) { // TODO 与 side 整合
		BlockPos secondary = from.immutable();
		BlockPos primary = to.immutable();
		if (this.wireConn.put(secondary, primary)) {
			this.wireConn.put(primary, secondary);
			this.link(primary, secondary);
			this.subscribedWire.get(from).forEach(player -> ApiNetworkManager.INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), new WireConnSyncPacket(from, to, false)));
			this.subscribedWire.get(to).forEach(player -> ApiNetworkManager.INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), new WireConnSyncPacket(to, from, false)));
		}
	}

	private void link(BlockPos primary, BlockPos secondary) {
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
		} else if (primaryComponent == null) {
			BlockPos secondaryNode = secondaryComponent.iterator().next();
			this.components.put(primary, secondaryComponent);
			secondaryComponent.add(primary);

			double outputOld = this.outputCollection.getOrDefault(secondaryNode, 0.0D);
			this.outputCollection.put(secondaryNode, outputOld + primaryOutput);
			double inputOld = this.inputCollection.getOrDefault(secondaryNode, 0.0D);
			this.inputCollection.put(secondaryNode, inputOld + primaryInput);
		} else if (secondaryComponent == null) {
			BlockPos primaryNode = primaryComponent.iterator().next();
			this.components.put(secondary, primaryComponent);
			primaryComponent.add(secondary);

			double outputOld = this.outputCollection.getOrDefault(primaryNode, 0.0D);
			this.outputCollection.put(primaryNode, outputOld + secondaryOutput);
			double inputOld = this.inputCollection.getOrDefault(primaryNode, 0.0D);
			this.inputCollection.put(primaryNode, inputOld + secondaryInput);
		} else if (primaryComponent != secondaryComponent) {
			BlockPos primaryNode = primaryComponent.iterator().next();
			BlockPos secondaryNode = secondaryComponent.iterator().next();
			Set<BlockPos> union = new LinkedHashSet<>(Sets.union(primaryComponent, secondaryComponent));
			union.forEach(pos -> this.components.put(pos, union));

			double outputDiff = this.outputCollection.getOrDefault(secondaryNode, 0.0D);
			double outputOld = this.outputCollection.getOrDefault(primaryNode, 0.0D);
			this.outputCollection.put(primaryNode, outputOld + outputDiff);
			this.outputCollection.remove(secondaryNode);

			double inputDiff = this.inputCollection.getOrDefault(secondaryNode, 0.0D);
			double inputOld = this.inputCollection.getOrDefault(primaryNode, 0.0D);
			this.inputCollection.put(primaryNode, inputOld + inputDiff);
			this.inputCollection.remove(secondaryNode);
		}
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
			BlockPos root = this.root(pos);
			if (updated.add(root)) {
				double power = this.outputCollection.getOrDefault(root, 0.0D);
				double energy = power - this.inputCollection.getOrDefault(root, 0.0D);
				if (energy > 0.0D) {
					forgeEnergy.add(root, (int) Math.floor(energy));
				}
			}

			Direction direction = entry.getValue();
			BlockPos target = pos.relative(direction);
			if (this.level.isAreaLoaded(target, 0)) {
				Optional.ofNullable(this.level.getBlockEntity(target)).ifPresent(blockEntity -> blockEntity.getCapability(ForgeCapabilities.ENERGY, direction.getOpposite()).ifPresent(capability -> {
					if (capability.canReceive()) {
						int diff = forgeEnergy.count(root);
						forgeEnergy.remove(root, capability.receiveEnergy(diff, false));
					}
				}));
			}
		}
	}

	private <T> List<T> shuffle(Collection<? extends T> iterable) {
		List<T> list = new ArrayList<>(iterable);
		Collections.shuffle(list, this.random);
		return list;
	}

	public class BFSIterator implements Iterator<BlockPos> {
		private final Set<BlockPos> searched = Sets.newLinkedHashSet();
		private final Queue<BlockPos> queue = Queues.newArrayDeque();

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

		public Set<BlockPos> getSearched() {
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
