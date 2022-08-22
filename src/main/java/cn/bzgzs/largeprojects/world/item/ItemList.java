package cn.bzgzs.largeprojects.world.item;

import cn.bzgzs.largeprojects.LargeProjects;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ItemList {
	public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, LargeProjects.MODID);

	public static final RegistryObject<Item> TEST = ITEMS.register("test", () -> new Item(new Item.Properties().tab(CreativeModeTabList.LARGEPROJECTS)));
}
