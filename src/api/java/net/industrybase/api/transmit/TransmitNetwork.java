package net.industrybase.api.transmit;

import net.industrybase.api.CapabilityList;
import com.google.common.collect.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.*;

public abstract class TransmitNetwork {
	protected final HashMap<BlockPos, LinkedHashSet<BlockPos>> components;
	protected final SetMultimap<BlockPos, Direction> connections;
	protected final LevelAccessor level;
	protected final ArrayDeque<Runnable> tasks;
	protected final HashMultiset<BlockPos> totalPower; // BlockPos 是中心块的坐标，出现个数为连通域总功率的数值
	protected final HashMultiset<BlockPos> totalResistance;
	protected final HashMap<BlockPos, Float> speeds;
	protected final HashMultiset<BlockPos> machinePower;
	protected final HashMultiset<BlockPos> machineResistance;

	public TransmitNetwork(LevelAccessor level) {
		this.components = new HashMap<>();
		this.connections = Multimaps.newSetMultimap(new HashMap<>(), () -> EnumSet.noneOf(Direction.class));
		this.level = level;
		this.tasks = new ArrayDeque<>();
		this.totalPower = HashMultiset.create();
		this.totalResistance = HashMultiset.create();
		this.speeds = new HashMap<>();
//		this.rotates = new HashMap<>();
//		this.roots = new HashMap<>();
		this.machinePower = HashMultiset.create();
		this.machineResistance = HashMultiset.create();
//		this.subscribes = HashMultimap.create();
	}

	public int size(BlockPos pos) {
		return this.components.containsKey(pos) ? this.components.get(pos).size() : 1;
	}

	public BlockPos root(BlockPos pos) {
		return this.components.containsKey(pos) ? this.components.get(pos).getFirst() : pos;
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

	public int getMachinePower(BlockPos pos) {
		return this.machinePower.count(pos);
	}

	public int setMachinePower(BlockPos pos, int power) {
		if (this.machinePower.count(pos) == power) return 0;
		int diff = power - this.machinePower.setCount(pos, power);
		BlockPos root = this.root(pos);
		if (this.components.containsKey(pos)) {
			if (diff >= 0) {
				this.totalPower.add(root, diff);
			} else {
				this.totalPower.remove(root, -diff);
			}
		}
		this.updateSpeed(root);
		return diff;
	}

	public int getMachineResistance(BlockPos pos) {
		return this.machineResistance.count(pos);
	}

	public int setMachineResistance(BlockPos pos, int resistance) {
		if (this.machineResistance.count(pos) == resistance) return 0;
		int diff = resistance - this.machineResistance.setCount(pos, resistance);
		BlockPos root = this.root(pos);
		if (this.components.containsKey(pos)) {
			if (diff >= 0) {
				this.totalResistance.add(root, diff);
			} else {
				this.totalResistance.remove(root, -diff);
			}
		}
		this.updateSpeed(root);
		return diff;
	}

	protected abstract void updateSpeed(BlockPos root);

	public void removeBlock(BlockPos pos) {
		this.tasks.offer(() -> {
			this.setMachinePower(pos, 0);
			this.setMachineResistance(pos, 0);
			for (Direction side : Direction.values()) {
				this.cut(pos, side);
			}
		});
	}

	protected void cut(BlockPos node, Direction direction) {
		if (this.connections.remove(node, direction)) {
			BlockPos another = node.relative(direction);
			this.connections.remove(another, direction.getOpposite());
			BFSIterator nodeIterator = new BFSIterator(node);
			BFSIterator anotherIterator = new BFSIterator(another);

			while (nodeIterator.hasNext()) {
				BlockPos next = nodeIterator.next();
				if (!anotherIterator.getSearched().contains(next)) {
					// exchange iterator，poll the connected domain
					BFSIterator iterator = anotherIterator;
					anotherIterator = nodeIterator;
					nodeIterator = iterator;
					continue;
				}
				return; // if two iterator contain the same block, which means domain keeps connecting, exit the loop
			}

			LinkedHashSet<BlockPos> primaryComponent = this.components.get(node);
			LinkedHashSet<BlockPos> secondaryComponent;
			BlockPos primaryRoot = primaryComponent.getFirst();
			LinkedHashSet<BlockPos> searched = nodeIterator.getSearched();

			if (searched.contains(primaryRoot)) {
				secondaryComponent = new LinkedHashSet<>(Sets.difference(primaryComponent, searched));
				primaryComponent.retainAll(searched); // keeps the root of primaryComponent isn't modified
			} else {
				secondaryComponent = searched;
				primaryComponent.removeAll(searched);
			}

			BlockPos secondaryRoot = secondaryComponent.getFirst();
			if (secondaryComponent.size() <= 1) {
				this.components.remove(secondaryRoot);

				int powerDiff = this.machinePower.count(secondaryRoot);
				int resistanceDiff = this.machineResistance.count(secondaryRoot);
				this.totalPower.remove(primaryRoot, powerDiff);
				this.totalResistance.remove(primaryRoot, resistanceDiff);
			} else {
				int powerDiff = 0;
				int resistanceDiff = 0;
				for (BlockPos pos : secondaryComponent) {
					this.components.put(pos, secondaryComponent);

					powerDiff += this.machinePower.count(pos);
					resistanceDiff += this.machineResistance.count(pos);
				}

				this.totalPower.remove(primaryRoot, powerDiff);
				this.totalResistance.remove(primaryRoot, resistanceDiff);
				this.totalPower.add(secondaryRoot, powerDiff);
				this.totalResistance.add(secondaryRoot, resistanceDiff);
			}
			if (primaryComponent.size() <= 1) {
				this.components.remove(primaryRoot);

				this.totalPower.setCount(primaryRoot, 0);
				this.totalResistance.setCount(primaryRoot, 0);
			}
			this.afterSplit(primaryRoot, secondaryRoot);
		}
	}

	protected abstract void afterSplit(BlockPos primaryRoot, BlockPos secondaryRoot);

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

	protected void link(BlockPos node, Direction direction) {
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

				this.afterMerge(secondary, primary);
			} else if (primaryComponent == null) {
				BlockPos secondaryRoot = secondaryComponent.getFirst();
				this.components.put(primary, secondaryComponent);
				secondaryComponent.add(primary);

				this.totalPower.add(secondaryRoot, primaryPower);
				this.totalResistance.add(secondaryRoot, primaryResistance);

				this.afterMerge(secondaryRoot, primary);
			} else if (secondaryComponent == null) {
				BlockPos primaryRoot = primaryComponent.getFirst();
				this.components.put(secondary, primaryComponent);
				primaryComponent.add(secondary);

				this.totalPower.add(primaryRoot, secondaryPower);
				this.totalResistance.add(primaryRoot, secondaryResistance);

				this.afterMerge(primaryRoot, secondary);
			} else if (primaryComponent != secondaryComponent) {
				BlockPos primaryRoot = primaryComponent.getFirst();
				BlockPos secondaryRoot = secondaryComponent.getFirst();
				secondaryComponent.forEach(pos -> { // TODO size
					primaryComponent.add(pos);
					this.components.put(pos, primaryComponent);
				});

				this.totalPower.add(primaryRoot, this.totalPower.setCount(secondaryRoot, 0));
				this.totalResistance.add(primaryRoot, this.totalResistance.setCount(secondaryRoot, 0));
				this.speeds.remove(secondaryRoot);

				this.afterMerge(primaryRoot, secondaryRoot);
			}
		}
	}

	protected abstract void afterMerge(BlockPos primaryRoot, BlockPos secondaryRoot);

	protected void tick() {
		for (Runnable runnable = this.tasks.poll(); runnable != null; runnable = this.tasks.poll()) {
			runnable.run();
		}
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

	@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE)
	public static class Manager {
		private static final IdentityHashMap<LevelAccessor, TransmitClientNetwork> INSTANCES_CLIENT = new IdentityHashMap<>();
		private static final IdentityHashMap<LevelAccessor, TransmitServerNetwork> INSTANCES_SERVER = new IdentityHashMap<>();

		public static TransmitNetwork get(LevelAccessor level) {
			if (level.isClientSide()) {
				return getClient(level);
			}
			return getServer(level);
		}

		public static TransmitClientNetwork getClient(LevelAccessor level) {
			return INSTANCES_CLIENT.computeIfAbsent(level, TransmitClientNetwork::new);
		}

		public static TransmitServerNetwork getServer(LevelAccessor level) {
			return INSTANCES_SERVER.computeIfAbsent(level, TransmitServerNetwork::new);
		}

		@SubscribeEvent
		public static void onUnload(LevelEvent.Unload event) {
			INSTANCES_CLIENT.remove(event.getLevel());
			INSTANCES_SERVER.remove(event.getLevel());
		}

		@SubscribeEvent
		public static void onLevelTick(TickEvent.LevelTickEvent event) {
			if (event.phase == TickEvent.Phase.START) {
				if (event.side.isClient()) {
					getClient(event.level).tick();
				} else {
					getServer(event.level).tick();
				}
			}
		}
	}
}
