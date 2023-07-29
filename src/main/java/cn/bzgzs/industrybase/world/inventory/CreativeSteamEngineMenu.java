package cn.bzgzs.industrybase.world.inventory;

import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class CreativeSteamEngineMenu extends AbstractContainerMenu {
	private final Container container;
	private final ContainerData data;

	public CreativeSteamEngineMenu(int id, Inventory playerInventory) {
		this(id, playerInventory, new SimpleContainer(1), new SimpleContainerData(1));
	}

	public CreativeSteamEngineMenu(int id, Inventory inventory, Container container, ContainerData data) {
		super(MenuTypeList.CREATIVE_STEAM_ENGINE.get(), id);
		this.container = container;
		this.data = data;
		// 执行检查
		checkContainerSize(container, 1);
		checkContainerDataCount(data, 1);

		this.addSlot(new Slot(container, 0, 70, 42) { // 燃料槽
			@Override
			public boolean mayPlace(ItemStack stack) {
				return false;
			}
		});

		// 玩家物品栏添加
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
		return ItemStack.EMPTY;
	}

	@Override
	public boolean stillValid(Player player) {
		return this.container.stillValid(player);
	}
}
