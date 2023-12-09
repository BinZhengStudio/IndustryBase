package cn.bzgzs.industrybase.api.electric;

import cn.bzgzs.industrybase.api.CapabilityList;
import cn.bzgzs.industrybase.api.network.ApiNetworkManager;
import cn.bzgzs.industrybase.api.network.server.RemoveWiresPacket;
import cn.bzgzs.industrybase.api.network.server.WireConnSyncPacket;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multimaps;
import com.google.common.collect.SetMultimap;
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
	private final ComponentMap components;
	private final SetMultimap<BlockPos, Direction> sideConn;
	private final HashMultimap<BlockPos, BlockPos> wireConn;
	private final LevelAccessor level;
	private final ArrayDeque<Runnable> tasks;
	private final ArrayList<Context> converted2EP;
	private final HashSet<Context> hasFE;
	private final EnergyMap machineEnergy;
	private final SetMultimap<BlockPos, Direction> FEMachines;
	private final HashMultimap<BlockPos, ServerPlayer> subscribes;

	public ElectricNetwork(LevelAccessor level) {
		this.random = new Random();
		this.machineEnergy = new EnergyMap();
		this.components = new ComponentMap(this.machineEnergy);
		this.sideConn = Multimaps.newSetMultimap(new HashMap<>(), () -> EnumSet.noneOf(Direction.class));
		this.wireConn = HashMultimap.create();
		this.level = level;
		this.tasks = new ArrayDeque<>();
		this.converted2EP = new ArrayList<>();
		this.hasFE = new HashSet<>();
		this.FEMachines = Multimaps.newSetMultimap(new HashMap<>(), () -> EnumSet.noneOf(Direction.class));
		this.subscribes = HashMultimap.create();
	}

	public int size(BlockPos pos) {
		return this.components.containsKey(pos) ? this.components.get(pos).getContext().size() : 1;
	}

	public double getTotalOutput(BlockPos pos) {
		return this.components.get(pos).getContext().getOutputEnergy();
	}

	public double getTotalInput(BlockPos pos) {
		return this.components.get(pos).getContext().getInputEnergy();
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
		return this.machineEnergy.get(pos).getOutputEnergy();
	}
	
	public double setMachineOutput(BlockPos pos, double power) {
		long powerLong = (long) Math.max(power * 100.0D, 0.0D);
		long outputOld = this.machineEnergy.get(pos).getOutput();
		if (outputOld == powerLong) return 0.0D;

		long diff = powerLong - outputOld;
		this.machineEnergy.addOutput(pos, diff);
		this.components.get(pos).getContext().addOutput(diff);
		return diff / 100.0D;
	}

	public double getMachineInput(BlockPos pos) {
		return this.machineEnergy.get(pos).getInputEnergy();
	}

	public double setMachineInput(BlockPos pos, double power) {
		long powerLong = (long) Math.max(power * 100.0D, 0.0D);
		long inputOld = this.machineEnergy.get(pos).getInput();
		if (inputOld == powerLong) return 0.0D;

		long diff = powerLong - inputOld;
		this.machineEnergy.addInput(pos, diff);
		this.components.get(pos).getContext().addInput(diff);
		return diff / 100.0D;
	}

	public double getRealInput(BlockPos pos) {
		Context context = this.components.get(pos).getContext();
		long totalOutput = context.getOutput() + (context.getFEInput() * 100L);
		long totalInput = context.getInput();
		double machineInput = this.machineEnergy.get(pos).getInputEnergy();
		if (totalInput > 0L) {
			if (totalOutput >= totalInput) {
				return machineInput;
			} else {
				return machineInput * totalOutput / totalInput;
			}
		}
		return 0.0D;
	}

	public int getFEEnergy(BlockPos pos) {
		return this.components.get(pos).getContext().getForgeEnergy();
	}

	public int getMaxFEStored(BlockPos pos) {
		return 100 * this.size(pos);
	}

	public int receiveFEEnergy(BlockPos pos, int maxReceive, boolean simulate) {
		Context context = this.components.get(pos).getContext();
		int receive = Math.min(maxReceive, this.getMaxFEStored(pos) - context.getForgeEnergy());
		if (!simulate) {
			context.addForgeEnergy(receive);
			this.hasFE.add(context);
		}
		return receive;
	}

	public int extractFEEnergy(BlockPos pos, int maxExtract, boolean simulate) {
		Context context = this.components.get(pos).getContext();
		int extract = Math.min(maxExtract, context.getForgeEnergy());
		if (!simulate) {
			context.addForgeEnergy(-extract);
			if (!context.hasForgeEnergy()) this.hasFE.remove(context);
		}
		return extract;
	}

	@SuppressWarnings("deprecation")
	public void removeBlock(BlockPos pos, Runnable callback) {
		this.tasks.offer(() -> {
			this.components.get(pos).getContext().shrinkEnergy(this.machineEnergy.remove(pos));
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

		HashSet<BlockPos> searched = nodeIterator.getSearched();
		Context primaryContext, secondaryContext = new Context(searched.iterator().next().hashCode(), searched.size());
		ContextWrapper wrapper = new ContextWrapper(1, secondaryContext);
		searched.forEach(pos -> {
			this.components.put(pos, wrapper);
			secondaryContext.add(this.machineEnergy.get(pos));
		});
		if (searched.contains(node)) {
			primaryContext = this.components.get(another).getContext().shrink(secondaryContext);
		} else {
			primaryContext = this.components.get(node).getContext().shrink(secondaryContext);
		}

		// 分配 FE 能量
		int primarySize = primaryContext.size(), secondarySize = secondaryContext.size();
		// TODO 整数直接相除？？？
		int diff = primaryContext.getForgeEnergy() * secondarySize / (primarySize + secondarySize);
		primaryContext.addForgeEnergy(-diff);
		secondaryContext.addForgeEnergy(diff);
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
		ContextWrapper primaryWrapper = this.components.getNullable(primary);
		ContextWrapper secondaryWrapper = this.components.getNullable(secondary);

		if (primaryWrapper == null && secondaryWrapper == null) {
			Energy primaryEnergy = this.machineEnergy.get(primary), secondaryEnergy = this.machineEnergy.get(secondary);
			ContextWrapper wrapper = new ContextWrapper(1, primary.hashCode(), 2, primaryEnergy, secondaryEnergy);
			this.components.put(primary, wrapper);
			this.components.put(secondary, wrapper);
		} else if (primaryWrapper == null) {
			secondaryWrapper.getContext().addEnergy(this.machineEnergy.get(primary)).addSize();
			this.components.put(primary, secondaryWrapper);
		} else if (secondaryWrapper == null) {
			primaryWrapper.getContext().addEnergy(this.machineEnergy.get(secondary)).addSize();
			this.components.put(secondary, primaryWrapper);
		} else if ((primaryWrapper = primaryWrapper.getLast()) != (secondaryWrapper = secondaryWrapper.getLast())) {
			Context primaryContext = primaryWrapper.getLastContext();
			Context secondaryContext = secondaryWrapper.getLastContext();
			int primaryLayer;

			if (primaryContext.size() <= 1) {
				secondaryContext.addAll(primaryContext);
				this.components.put(primary, secondaryWrapper);
			} else if (secondaryContext.size() <= 1) {
				primaryContext.addAll(secondaryContext);
				this.components.put(secondary, primaryWrapper);
			} else if ((primaryLayer = primaryWrapper.layer()) > secondaryWrapper.layer()) {
				primaryContext.addAll(secondaryContext);
				secondaryWrapper.setContext(primaryWrapper);
			} else {
				secondaryContext.addAll(primaryContext);
				primaryWrapper.setContext(secondaryWrapper);
				secondaryWrapper.setLayer(primaryLayer + 1);
			}
		}
	}

	private void tickStart() {
//		long st = System.currentTimeMillis();
//		boolean flag = this.tasks.isEmpty();
		for (Runnable runnable = this.tasks.poll(); runnable != null; runnable = this.tasks.poll()) {
			runnable.run();
		}
//		if (!flag) System.out.println("Time: " + (System.currentTimeMillis() - st));
	}

	@SuppressWarnings("deprecation")
	private void tickEnd() {
		HashSet<Context> updated = new HashSet<>();
		HashMultiset<Context> forgeEnergy = HashMultiset.create();
		for (Map.Entry<BlockPos, Direction> entry : this.shuffle(this.FEMachines.entries())) {
			BlockPos pos = entry.getKey();
			Direction direction = entry.getValue();
			BlockPos target = pos.relative(direction);
			if (this.level.isAreaLoaded(target, 0)) { // TODO 需要检查吗？
				Context context = this.components.get(pos).getContext();
				// 将剩余 EP 转换为 FE
				if (updated.add(context)) { // 已转换过的能量网络则跳过
					long excess = context.getOutput() - context.getInput();
					if (excess > 0L) {
						forgeEnergy.add(context, (int) Math.floor(excess / 100.0D));
					}
				}

				// 向 FE 方块输出能量
				BlockEntity blockEntity;
				if ((blockEntity = this.level.getBlockEntity(target)) != null) {
					blockEntity.getCapability(ForgeCapabilities.ENERGY, direction.getOpposite()).ifPresent(capability -> {
						if (capability.canReceive()) {
							int diff = forgeEnergy.count(context);
							int FEDiff = context.getForgeEnergy();
							forgeEnergy.remove(context, capability.receiveEnergy(diff, false));
							if (forgeEnergy.count(context) <= 0) { // 先将新转化的 FE 分配完
								context.addForgeEnergy(-capability.receiveEnergy(FEDiff, false));
							}
							// 由 EP 转化的能量，多余的均丢弃
						}
					});
				}
			}
		}

		// 将未使用 FE 转化为 EP
		Iterator<Context> iterator = this.converted2EP.iterator();
		while (iterator.hasNext()) { // 清除先前转换的 EP
			iterator.next().clearFEInput();
			iterator.remove();
		}
		this.hasFE.forEach(context -> {
			long lack = context.getInput() - context.getOutput();
			if (lack > 0L) {
				context.convertFEInput((int) Math.ceil(lack / 100.0D));
				this.converted2EP.add(context);
			}
		});
	}

	private <T> List<T> shuffle(Collection<? extends T> iterable) {
		List<T> list = new ArrayList<>(iterable);
		Collections.shuffle(list, this.random);
		return list;
	}

	public class BFSIterator implements Iterator<BlockPos> {
		private final HashSet<BlockPos> searched = new HashSet<>();
		private final ArrayDeque<BlockPos> queue = new ArrayDeque<>();

		public BFSIterator(BlockPos node) {
			node = node.immutable();
			this.searched.add(node);
			this.queue.offer(node);
		}

		public boolean hasNext() {
			return !this.queue.isEmpty();
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

		public HashSet<BlockPos> getSearched() {
			return this.searched;
		}
	}

	@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE)
	public static class Manager {
		private static final IdentityHashMap<LevelAccessor, ElectricNetwork> INSTANCES = new IdentityHashMap<>();

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
