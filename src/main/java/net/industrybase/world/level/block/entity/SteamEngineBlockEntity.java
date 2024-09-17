package net.industrybase.world.level.block.entity;

import net.industrybase.api.IndustryBaseApi;
import net.industrybase.api.pipe.PipeConnectedHandler;
import net.industrybase.api.pipe.StorageInterface;
import net.industrybase.api.transmit.MechanicalTransmit;
import net.industrybase.network.server.WaterAmountPayload;
import net.industrybase.world.inventory.SteamEngineMenu;
import net.industrybase.world.level.block.SteamEngineBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.neoforged.neoforge.common.NeoForgeMod;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.Nullable;

public class SteamEngineBlockEntity extends BaseContainerBlockEntity implements WorldlyContainer {
	private int burnTime;
	private int totalBurnTime;
	private int shrinkTick;
	public static final int MAX_POWER = 100;
	public static final int MAX_WATER = FluidType.BUCKET_VOLUME * 2;
	private NonNullList<ItemStack> inventory = NonNullList.withSize(1, ItemStack.EMPTY);
	private final PipeConnectedHandler handler = new PipeConnectedHandler(this);
	private final FluidTank tank = new FluidTank(MAX_WATER, fluidStack -> fluidStack.is(NeoForgeMod.WATER_TYPE.value())) {
		@Override
		protected void onContentsChanged() {
			if (level != null && !level.isClientSide) {
				// send packet to sync the fluid amount
				PacketDistributor.sendToAllPlayers(new WaterAmountPayload(worldPosition, tank.getFluidAmount()));
				for (Direction direction : Direction.values()) {
					if (direction == Direction.UP) {
						handler.setPressure(direction, 0.0D);
					} else {
						handler.setPressure(direction, (double) this.getFluidAmount() / this.getCapacity());
					}
				}
			}
		}
	};
	private final MechanicalTransmit transmit = new MechanicalTransmit(this);
	private int oldWaterAmount;
	private int waterAmount; // 仅在客户端调用
	private final ContainerData data = new ContainerData() { // 用于双端同步数据
		@Override
		public int get(int index) {
			return switch (index) {
				case 0 -> SteamEngineBlockEntity.this.transmit.getPower();
				case 1 -> (int) (SteamEngineBlockEntity.this.transmit.getSpeed() * 100);
				case 2 -> burnTime;
				case 3 -> totalBurnTime;
				case 4 -> tank.getFluidAmount();
				default -> 0;
			};
		}

		@Override
		public void set(int index, int value) {
			switch (index) {
				case 0 -> SteamEngineBlockEntity.this.transmit.setPower(value);
				case 2 -> burnTime = value;
				case 3 -> totalBurnTime = value;
				default -> {
				}
			}
		}

		@Override
		public int getCount() {
			return 5;
		}
	};

	public SteamEngineBlockEntity(BlockPos pos, BlockState state) {
		super(BlockEntityTypeList.STEAM_ENGINE.get(), pos, state);
	}

	public static void clientTick(Level level, BlockPos pos, BlockState state, SteamEngineBlockEntity blockEntity) {
		blockEntity.oldWaterAmount = blockEntity.waterAmount;
	}

	public static void serverTick(Level level, BlockPos pos, BlockState state, SteamEngineBlockEntity blockEntity) {
		boolean flag = false; // 是否有数据改变

		if (blockEntity.isLit()) { // 输出能量
			--blockEntity.burnTime; // 减少燃烧时间

			if (!blockEntity.tank.isEmpty()) {
				if (blockEntity.shrinkTick <= 0) { // 消耗水
					blockEntity.tank.drain(1, IFluidHandler.FluidAction.EXECUTE);
					blockEntity.shrinkTick = 6; // 每 6tick 减一次 waterAmount，这样水不会少的太快
				} else {
					--blockEntity.shrinkTick;
				}
				if (blockEntity.transmit.getPower() < MAX_POWER) { // 增加功率，使之达到最大
					blockEntity.transmit.setPower(blockEntity.transmit.getPower() + 1);
				}
			} else {
				if (blockEntity.transmit.getPower() > 0) {
					blockEntity.transmit.setPower(blockEntity.transmit.getPower() - 1);
				}
			}
			flag = true;
		} else {
			if (blockEntity.transmit.getPower() > 0) {
				blockEntity.transmit.setPower(blockEntity.transmit.getPower() - 1);
				flag = true;
			}
		}

		if (!blockEntity.isLit() && !blockEntity.tank.isEmpty()) { // 如果没有燃烧，并且有水，则消耗燃料并燃烧
			ItemStack stack = blockEntity.inventory.getFirst();
			int time = stack.getBurnTime(RecipeType.SMELTING);
			if (time > 0) {
				flag = true;
				blockEntity.burnTime = time;
				blockEntity.totalBurnTime = time;
				if (stack.hasCraftingRemainingItem()) {
					blockEntity.inventory.set(0, stack.getCraftingRemainingItem());
				} else if (!stack.isEmpty()) {
					stack.shrink(1);
					if (stack.isEmpty()) {
						blockEntity.inventory.set(0, stack.getCraftingRemainingItem());
					}
				}
			}
		}

		if (blockEntity.isLit() != state.getValue(SteamEngineBlock.LIT)) { // 如果燃烧状态与state不符，则更新state
			level.setBlock(pos, state.setValue(SteamEngineBlock.LIT, blockEntity.isLit()), 3);
			flag = true;
		}

		if (flag) {
			blockEntity.setChanged();
		}
	}

	public boolean isLit() { // 是否正在燃烧
		return this.burnTime > 0;
	}

	public static boolean isFuel(ItemStack stack) {
		return stack.getBurnTime(RecipeType.SMELTING) > 0;
	}

	@Override
	public void onLoad() {
		super.onLoad();
		this.transmit.register();
		this.transmit.setResistance(10);
		this.handler.registerHandler(new StorageInterface(this.tank::getCapacity, this.tank::getFluidAmount, this.tank::fill, this.tank::drain));
	}

	@Override
	protected Component getDefaultName() {
		return Component.translatable("container." + IndustryBaseApi.MODID + ".steam_engine");
	}

	@Override
	protected NonNullList<ItemStack> getItems() {
		return this.inventory;
	}

	@Override
	protected void setItems(NonNullList<ItemStack> items) {
		this.inventory = items;
	}

	@Override
	protected AbstractContainerMenu createMenu(int id, Inventory inventory) {
		return new SteamEngineMenu(id, inventory, this, this.data);
	}

	public int getWaterAmount() {
		return this.waterAmount;
	}

	public int getOldWaterAmount() {
		return this.oldWaterAmount;
	}

	public void setClientWaterAmount(int waterAmount) {
		this.oldWaterAmount = this.waterAmount;
		this.waterAmount = waterAmount;
	}

	@Nullable
	public MechanicalTransmit getTransmit(Direction side) {
		if (side.getAxis() == this.getBlockState().getValue(BlockStateProperties.AXIS)) {
			return this.transmit;
		}
		return null;
	}

	@Nullable
	public FluidTank getTank(Direction side) {
		if (side.getAxis() != this.getBlockState().getValue(BlockStateProperties.AXIS)) {
			return this.tank;
		}
		return null;
	}

	@Override
	public void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
		super.loadAdditional(tag, registries);
		this.transmit.readFromNBT(tag);
		ContainerHelper.loadAllItems(tag, this.inventory, registries);
		this.burnTime = tag.getInt("BurnTime");
		this.totalBurnTime = tag.getInt("TotalBurnTime");
		this.shrinkTick = tag.getInt("ShrinkTick");
		this.tank.readFromNBT(registries, tag);
	}

	@Override
	protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
		super.saveAdditional(tag, registries);
		this.transmit.writeToNBT(tag);
		ContainerHelper.saveAllItems(tag, this.inventory, registries);
		tag.putInt("BurnTime", this.burnTime);
		tag.putInt("TotalBurnTime", this.totalBurnTime);
		tag.putInt("ShrinkTick", this.shrinkTick);
		this.tank.writeToNBT(registries, tag);
	}

	@Override
	public void setRemoved() {
		this.transmit.remove();
		this.handler.removeHandler();
		super.setRemoved();
	}

	@Override
	public int getContainerSize() {
		return this.inventory.size();
	}

	@Override
	public boolean isEmpty() {
		return this.inventory.isEmpty();
	}

	@Override
	public ItemStack getItem(int index) {
		return this.inventory.get(index);
	}

	@Override
	public ItemStack removeItem(int index, int amount) {
		return ContainerHelper.removeItem(this.inventory, index, amount);
	}

	@Override
	public ItemStack removeItemNoUpdate(int index) {
		return ContainerHelper.takeItem(this.inventory, index);
	}

	@Override
	public void setItem(int index, ItemStack itemStack) {
		this.inventory.set(index, itemStack);
		if (itemStack.getCount() > this.getMaxStackSize()) {
			itemStack.setCount(this.getMaxStackSize());
		}
	}

	@Override
	public boolean stillValid(Player player) {
		if (this.level.getBlockEntity(this.worldPosition) != this) {
			return false;
		} else {
			return player.distanceToSqr(this.worldPosition.getX() + 0.5D, this.worldPosition.getY() + 0.5D, this.worldPosition.getZ() + 0.5D) <= 64.0D;
		}
	}

	@Override
	public void clearContent() {
		this.inventory.clear();
	}

	@Override
	public int[] getSlotsForFace(Direction side) {
		return new int[]{0};
	}

	@Override
	public boolean canPlaceItem(int index, ItemStack stack) {
		return isFuel(stack) || stack.is(Items.BUCKET);
	}

	@Override
	public boolean canPlaceItemThroughFace(int index, ItemStack stack, @Nullable Direction direction) {
		return this.canPlaceItem(index, stack);
	}

	@Override
	public boolean canTakeItemThroughFace(int index, ItemStack stack, Direction direction) {
		return stack.is(Items.WATER_BUCKET) || stack.is(Items.BUCKET);
	}
}
