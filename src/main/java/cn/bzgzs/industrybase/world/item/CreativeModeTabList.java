package cn.bzgzs.industrybase.world.item;

import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;

public class CreativeModeTabList { // 创造模式物品栏
	public static final CreativeModeTab INDUSTRYBASE = new CreativeModeTab("industrybase") {
		@Override
		public ItemStack makeIcon() {
			return new ItemStack(ItemList.DYNAMO.get());
		}
	};
}
