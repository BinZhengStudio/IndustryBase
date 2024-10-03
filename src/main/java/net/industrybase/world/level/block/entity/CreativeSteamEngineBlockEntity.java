package net.industrybase.world.level.block.entity;

import net.industrybase.api.IndustryBaseApi;
import net.industrybase.api.pipe.PipeConnectedHandler;
import net.industrybase.api.pipe.StorageInterface;
import net.industrybase.api.transmit.MechanicalTransmit;
import net.industrybase.world.inventory.CreativeSteamEngineMenu;
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
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.neoforged.neoforge.common.NeoForgeMod;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import org.jetbrains.annotations.Nullable;

public class CreativeSteamEngineBlockEntity extends BaseContainerBlockEntity implements WorldlyContainer {
	private NonNullList<ItemStack> inventory = NonNullList.withSize(1, new ItemStack(Items.LAVA_BUCKET));
	private final PipeConnectedHandler handler = new PipeConnectedHandler(this);
	private final FluidTank tank = new FluidTank(2000, fluidStack -> fluidStack.is(NeoForgeMod.WATER_TYPE.value()));

	private final MechanicalTransmit transmit = new MechanicalTransmit(this);
	private final ContainerData data = new ContainerData() { // 用于双端同步数据
		@Override
		public int get(int index) {
			return index == 0 ? (int) (CreativeSteamEngineBlockEntity.this.transmit.getSpeed() * 100) : 0;
		}

		@Override
		public void set(int index, int value) {
		}

		@Override
		public int getCount() {
			return 1;
		}
	};

	public CreativeSteamEngineBlockEntity(BlockPos pos, BlockState state) {
		super(BlockEntityTypeList.CREATIVE_STEAM_ENGINE.get(), pos, state);
	}

	@Override
	public void onLoad() {
		super.onLoad();
		this.transmit.register();
		this.transmit.setPower(100);
		this.transmit.setResistance(10);
		this.handler.registerHandler(new StorageInterface(this.tank::getCapacity, this.tank::getFluidAmount, this.tank::fill, this.tank::drain));
	}

	@Override
	protected Component getDefaultName() {
		return Component.translatable("container." + IndustryBaseApi.MODID + ".creative_steam_engine");
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
		return new CreativeSteamEngineMenu(id, inventory, this, this.data);
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
	}

	@Override
	protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
		super.saveAdditional(tag, registries);
		this.transmit.writeToNBT(tag);
		ContainerHelper.saveAllItems(tag, this.inventory, registries);
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
		return false;
	}

	@Override
	public ItemStack getItem(int index) {
		return this.inventory.get(index);
	}

	@Override
	public ItemStack removeItem(int index, int amount) {
		return ItemStack.EMPTY;
	}

	@Override
	public ItemStack removeItemNoUpdate(int index) {
		return ItemStack.EMPTY;
	}

	@Override
	public void setItem(int index, ItemStack itemStack) {
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
		return false;
	}

	@Override
	public boolean canPlaceItemThroughFace(int index, ItemStack stack, @Nullable Direction direction) {
		return this.canPlaceItem(index, stack);
	}

	@Override
	public boolean canTakeItemThroughFace(int index, ItemStack stack, Direction direction) {
		return false;
	}
}
