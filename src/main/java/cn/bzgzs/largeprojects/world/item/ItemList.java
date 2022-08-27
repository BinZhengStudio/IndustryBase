package cn.bzgzs.largeprojects.world.item;

import cn.bzgzs.largeprojects.LargeProjects;
import cn.bzgzs.largeprojects.world.level.block.BlockList;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ItemList {
	public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, LargeProjects.MODID);

	public static final RegistryObject<BlockItem> DYNAMO = ITEMS.register("dynamo", () -> new BlockItem(BlockList.DYNAMO.get(), new Item.Properties().tab(CreativeModeTabList.LARGEPROJECTS)));
	public static final RegistryObject<BlockItem> TRANSMISSION_ROD = ITEMS.register("transmission_rod", () -> new BlockItem(BlockList.TRANSMISSION_ROD.get(), new Item.Properties().tab(CreativeModeTabList.LARGEPROJECTS)));
}
