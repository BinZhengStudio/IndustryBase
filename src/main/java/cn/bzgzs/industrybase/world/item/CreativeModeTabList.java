package cn.bzgzs.industrybase.world.item;

import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;

public class CreativeModeTabList {
	public static final CreativeModeTab INDUSTRYBASE = new CreativeModeTab("industrybase") {
		@Override
		public ItemStack makeIcon() {
			return new ItemStack(ItemList.DYNAMO.get());
		}
	};
}
