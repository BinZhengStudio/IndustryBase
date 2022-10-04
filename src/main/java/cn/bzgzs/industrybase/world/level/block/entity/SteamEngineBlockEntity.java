package cn.bzgzs.industrybase.world.level.block.entity;

import cn.bzgzs.industrybase.IndustryBase;
import cn.bzgzs.industrybase.api.CapabilityList;
import cn.bzgzs.industrybase.api.world.level.block.entity.ContainerTransmitBlockEntity;
import cn.bzgzs.industrybase.world.inventory.SteamEngineMenu;
import cn.bzgzs.industrybase.world.level.block.SteamEngineBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
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
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.WaterFluid;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidType;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SteamEngineBlockEntity extends ContainerTransmitBlockEntity implements WorldlyContainer {
	private int burnTime;
	private int totalBurnTime;
	private int shrinkTick;
	public static final int MAX_POWER = 100;
	public static final int MAX_WATER = FluidType.BUCKET_VOLUME * 2;
	private final NonNullList<ItemStack> inventory = NonNullList.withSize(1, ItemStack.EMPTY);
	private final FluidTank tank = new FluidTank(MAX_WATER, fluidStack -> fluidStack.getFluid() instanceof WaterFluid);
	private final LazyOptional<IFluidHandler> fluidHandler = LazyOptional.of(() -> tank);
	private final ContainerData data = new ContainerData() { // 用于向客户端发送服务端的相关数据
		@Override
		public int get(int index) {
			return switch (index) {
				case 0 -> getPower();
				case 1 -> (int) (getSpeed() * 100);
				case 2 -> burnTime;
				case 3 -> totalBurnTime;
				case 4 -> tank.getFluidAmount();
				default -> 0;
			};
		}

		@Override
		public void set(int index, int value) { // 这个是让在服务端的Menu中也能更改BlockEntity的数据
			switch (index) {
				case 0 -> setPower(value);
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

	public static void serverTick(Level level, BlockPos pos, BlockState state, SteamEngineBlockEntity blockEntity) {
		boolean flag = false; // 是否需要setChanged

		if (blockEntity.isLit()) { // 输出能量
			--blockEntity.burnTime; // 减少燃烧时间

			if (!blockEntity.tank.isEmpty()) {
				if (blockEntity.shrinkTick <= 0) { // 消耗水
					blockEntity.tank.drain(1, IFluidHandler.FluidAction.EXECUTE);
					blockEntity.shrinkTick = 6; // 每 6tick 减一次 waterAmount，这样水不会少的太快
				} else {
					--blockEntity.shrinkTick;
				}

				// TODO
//				if (blockEntity.getPower() < MAX_POWER) { // 增加功率，使之达到最大
//					blockEntity.setPower(blockEntity.getPower() + 1);
//				}
			}
			// TODO
			if (blockEntity.getPower() < MAX_POWER) { // 增加功率，使之达到最大
				blockEntity.setPower(blockEntity.getPower() + 1);
			}
			flag = true;
		}

		if (!blockEntity.isLit() /* || blockEntity.tank.isEmpty() */) { // TODO 小细节待修复
			if (blockEntity.getPower() > 0) {
				blockEntity.setPower(blockEntity.getPower() - 1);
				flag = true;
			}
		}

		if (!blockEntity.isLit() /* TODO && !blockEntity.tank.isEmpty() */) { // 如果没有燃烧，并且有水，则消耗燃料并燃烧
			ItemStack stack = blockEntity.inventory.get(0);
			int time = ForgeHooks.getBurnTime(stack, RecipeType.SMELTING);
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
		return ForgeHooks.getBurnTime(stack, RecipeType.SMELTING) > 0;
	}

	@Override
	public void onLoad() {
		super.onLoad();
		this.setResistance(5);
	}

	@Override
	protected Component getDefaultName() {
		return Component.translatable("container." + IndustryBase.MODID + ".steam_engine");
	}

	@Override
	protected AbstractContainerMenu createMenu(int id, Inventory inventory) {
		return new SteamEngineMenu(id, inventory, this, this.data);
	}

	@Override
	public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
		if (side != null) {
			if (side.getAxis() == this.getBlockState().getValue(SteamEngineBlock.AXIS)) {
				return cap == CapabilityList.MECHANICAL_TRANSMIT ? this.getTransmit().cast() : super.getCapability(cap, side);
			} else {
				return cap == ForgeCapabilities.FLUID_HANDLER ? this.fluidHandler.cast() : super.getCapability(cap, side);
			}
		}
		return super.getCapability(cap, null);
	}

	@Override
	public void load(CompoundTag tag) {
		super.load(tag);
		ContainerHelper.loadAllItems(tag, this.inventory);
		this.burnTime = tag.getInt("BurnTime");
		this.totalBurnTime = tag.getInt("TotalBurnTime");
		this.shrinkTick = tag.getInt("ShrinkTick");
		this.tank.readFromNBT(tag);
	}

	@Override
	protected void saveAdditional(CompoundTag tag) {
		super.saveAdditional(tag);
		ContainerHelper.saveAllItems(tag, this.inventory);
		tag.putInt("BurnTime", this.burnTime);
		tag.putInt("TotalBurnTime", this.totalBurnTime);
		tag.putInt("ShrinkTick", this.shrinkTick);
		this.tank.writeToNBT(tag);
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
