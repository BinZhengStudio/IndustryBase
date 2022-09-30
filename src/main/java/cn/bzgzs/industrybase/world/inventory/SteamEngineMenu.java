package cn.bzgzs.industrybase.world.inventory;

import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraftforge.common.ForgeHooks;

public class SteamEngineMenu extends AbstractContainerMenu {
	private final Container container;
	private final ContainerData data;

	public SteamEngineMenu(int id, Inventory playerInventory) {
		this(id, playerInventory, new SimpleContainer(2), new SimpleContainerData(9));
	}

	public SteamEngineMenu(int id, Inventory inventory, Container container, ContainerData data) {
		super(MenuTypeList.STEAM_ENGINE.get(), id);
		this.container = container;
		this.data = data;
		checkContainerSize(container, 1);
		checkContainerDataCount(data, 3);

		this.addSlot(new Slot(container, 0, 70, 42) {
			@Override
			public boolean mayPlace(ItemStack stack) {
				return isFuel(stack) || FurnaceFuelSlot.isBucket(stack);
			}

			@Override
			public int getMaxStackSize(ItemStack stack) {
				return FurnaceFuelSlot.isBucket(stack) ? 1 : super.getMaxStackSize(stack);
			}
		}); // 燃料槽

		for (int i = 0; i < 3; ++i) {
			for (int j = 0; j < 9; ++j) {
				this.addSlot(new Slot(inventory, j + i * 9 + 9, 8 + j * 18, 84 + i * 18));
			}
		}

		for (int i = 0; i < 9; ++i) {
			this.addSlot(new Slot(inventory, i, 8 + i * 18, 142));
		}

		this.addDataSlots(data);
	}

	public ContainerData getData() {
		return this.data;
	}

	@Override
	public ItemStack quickMoveStack(Player player, int index) {
		ItemStack stack = ItemStack.EMPTY;
		Slot slot = this.getSlot(index);
		if (slot.hasItem()) {
			ItemStack stack1 = slot.getItem();
			stack = stack1.copy();
			if (index < 1) {
				if (!this.moveItemStackTo(stack1, 1, this.slots.size(), true)) {
					return ItemStack.EMPTY;
				}
			} else if (isFuel(stack1)) {
				if (!this.moveItemStackTo(stack1, 0, 1, false)) {
					return ItemStack.EMPTY;
				}
			}

			if (stack1.isEmpty()) {
				slot.set(ItemStack.EMPTY);
			} else {
				slot.setChanged();
			}

			if (stack1.getCount() == stack.getCount()) {
				return ItemStack.EMPTY;
			}

			slot.onTake(player, stack1);
		}
		return stack;
	}

	public static boolean isFuel(ItemStack pStack) {
		return ForgeHooks.getBurnTime(pStack, RecipeType.SMELTING) > 0;
	}

	@Override
	public boolean stillValid(Player player) {
		return this.container.stillValid(player);
	}
}
