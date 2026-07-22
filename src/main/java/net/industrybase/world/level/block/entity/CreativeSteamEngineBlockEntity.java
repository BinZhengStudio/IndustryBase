package net.industrybase.world.level.block.entity;

import org.jspecify.annotations.Nullable;

import net.industrybase.capability.IndustryBaseApi;
import net.industrybase.capability.transmit.MechanicalTransmit;
import net.industrybase.world.inventory.CreativeSteamEngineMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

public class CreativeSteamEngineBlockEntity extends SteamEngineBlockEntity {
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
		this.transmit.setPower(100);
	}

	@Override
	protected Component getDefaultName() {
		return Component.translatable("container." + IndustryBaseApi.MODID + ".creative_steam_engine");
	}

	@Override
	protected void setItems(NonNullList<ItemStack> items) {
	}

	@Override
	protected AbstractContainerMenu createMenu(int id, Inventory inventory) {
		return new CreativeSteamEngineMenu(id, inventory, this, this.data);
	}

	@Nullable
	public MechanicalTransmit getTransmit(Direction side) {
		if (side != null && side.getAxis() == this.getBlockState().getValue(BlockStateProperties.AXIS)) {
			return this.transmit;
		}
		return null;
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
	public boolean canPlaceItem(int index, ItemStack stack) {
		return false;
	}

	@Override
	public boolean canTakeItemThroughFace(int index, ItemStack stack, Direction direction) {
		return false;
	}
}
