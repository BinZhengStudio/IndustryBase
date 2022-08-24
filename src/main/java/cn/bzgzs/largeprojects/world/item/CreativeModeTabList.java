package cn.bzgzs.largeprojects.world.item;

import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;

public class CreativeModeTabList {
	public static final CreativeModeTab LARGEPROJECTS = new CreativeModeTab("largeprojects") {
		@Override
		public ItemStack makeIcon() {
			return new ItemStack(ItemList.DYNAMO.get());
		}
	};
}
