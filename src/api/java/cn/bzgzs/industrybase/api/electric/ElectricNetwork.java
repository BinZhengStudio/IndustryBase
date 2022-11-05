package cn.bzgzs.industrybase.api.electric;

import cn.bzgzs.industrybase.api.CapabilityList;
import com.google.common.collect.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

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
	}

	public int size(BlockPos pos) {
		return this.components.containsKey(pos) ? this.components.get(pos).size() : 1;
	}

	public BlockPos root(BlockPos pos) {
		return this.components.containsKey(pos) ? this.components.get(pos).iterator().next() : pos;
	}

	public double totalOutput(BlockPos pos) {
		BlockPos root = this.root(pos);
		return this.outputCollection.getOrDefault(root, 0.0D);
	}

	public double totalInput(BlockPos pos) {
		BlockPos root = this.root(pos);
		return this.inputCollection.getOrDefault(root, 0.0D);
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
			this.outputCollection.put(root, this.outputCollection.getOrDefault(root, 0.0D) + diff);
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
			this.inputCollection.put(root, this.inputCollection.getOrDefault(root, 0.0D) + diff);
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
			for (BlockPos another : this.wireConn.get(pos)) {
				this.cutWire(pos, another);
			}
			this.components.remove(pos);
			this.outputCollection.remove(pos);
			this.inputCollection.remove(pos);
			callback.run();
		});
	}

	public void removeWire(BlockPos from, BlockPos to) {
		this.tasks.offer(() -> this.cutWire(from, to));
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
		// TODO remove component
		double outputDiff = 0.0D;
		double inputDiff = 0.0D;
		for (BlockPos pos : secondaryComponent) {
			this.components.put(pos, secondaryComponent);

			outputDiff += this.machineOutput.getOrDefault(pos, 0.0D);
			inputDiff += this.machineInput.getOrDefault(pos, 0.0D);

		}
		double outputOld = this.outputCollection.getOrDefault(primaryNode, 0.0D);
		double inputOld = this.inputCollection.getOrDefault(primaryNode, 0.0D);
		this.outputCollection.put(primaryNode, outputOld - outputDiff);
		this.inputCollection.put(primaryNode, inputOld - inputDiff);
		this.outputCollection.put(secondaryNode, outputDiff);
		this.inputCollection.put(secondaryNode, inputDiff);
	}

	public void addOrChangeBlock(BlockPos pos, Runnable callback) {
		this.tasks.offer(() -> {
			for (Direction side : Direction.values()) {
				if (this.hasElectricalCapability(pos, side)) {
					if (!this.components.containsKey(pos)) {
						Set<BlockPos> initSet = new LinkedHashSet<>();
						initSet.add(pos);
						this.components.put(pos, initSet);
					}
					if (this.hasElectricalCapability(pos.relative(side), side.getOpposite())) {
						BlockPos another = pos.relative(side);
						if (!this.components.containsKey(another)) {
							Set<BlockPos> initSet = new LinkedHashSet<>();
							initSet.add(another);
							this.components.put(another, initSet);
						}
						this.FEMachines.remove(pos, side);
						this.linkSide(pos, side);
					} else if (this.hasFECapability(pos.relative(side), side.getOpposite())) {
						this.FEMachines.put(pos, side);
						this.cutSide(pos, side);
					}
				} else {
					this.FEMachines.remove(pos, side);
					this.cutSide(pos, side);
				}
			}
			callback.run();
		});
	}

	public boolean addWire(BlockPos from, BlockPos to, Runnable callback) {
		if (!from.equals(to) && this.wireConn.containsEntry(from, to)) {
			return this.tasks.offer(() -> {
				linkWire(from, to);
				callback.run();
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
				union.forEach(pos -> {
					this.components.put(pos, union);
				});

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
	}

	private void linkWire(BlockPos from, BlockPos to) {
		BlockPos secondary = from.immutable();
		BlockPos primary = to.immutable();
		if (this.wireConn.put(secondary, primary)) {
			this.wireConn.put(primary, secondary);
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
				union.forEach(pos -> {
					this.components.put(pos, union);
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
				if (power > 0.0D) {
					double energy = power - this.inputCollection.getOrDefault(root, 0.0D);
					if (energy > 0.0D) {
						forgeEnergy.add(root, (int) Math.floor(energy));
					}
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
