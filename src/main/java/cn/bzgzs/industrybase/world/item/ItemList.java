package cn.bzgzs.industrybase.world.item;

import cn.bzgzs.industrybase.IndustryBase;
import cn.bzgzs.industrybase.world.level.block.BlockList;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ItemList {
	public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, IndustryBase.MODID);

//	public static final RegistryObject<Item> WIRE_COIL = ITEMS.register("wire_coil", WireCoilItem::new);

	public static final RegistryObject<BlockItem> DYNAMO = ITEMS.register("dynamo", () -> new BlockItem(BlockList.DYNAMO.get(), new Item.Properties().tab(CreativeModeTabList.INDUSTRYBASE)));
	public static final RegistryObject<BlockItem> IRON_TRANSMISSION_ROD = ITEMS.register("iron_transmission_rod", () -> new BlockItem(BlockList.IRON_TRANSMISSION_ROD.get(), new Item.Properties().tab(CreativeModeTabList.INDUSTRYBASE)));
	public static final RegistryObject<BlockItem> GOLD_TRANSMISSION_ROD = ITEMS.register("gold_transmission_rod", () -> new BlockItem(BlockList.GOLD_TRANSMISSION_ROD.get(), new Item.Properties().tab(CreativeModeTabList.INDUSTRYBASE)));
	public static final RegistryObject<BlockItem> STEAM_ENGINE = ITEMS.register("steam_engine", () -> new BlockItem(BlockList.STEAM_ENGINE.get(), new Item.Properties().tab(CreativeModeTabList.INDUSTRYBASE)));
	public static final RegistryObject<BlockItem> AXIS_CONNECTOR = ITEMS.register("axis_connector", () -> new BlockItem(BlockList.AXIS_CONNECTOR.get(), new Item.Properties().tab(CreativeModeTabList.INDUSTRYBASE)));
//	public static final RegistryObject<BlockItem> WIRE = ITEMS.register("wire", () -> new BlockItem(BlockList.WIRE.get(), new Item.Properties().tab(CreativeModeTabList.INDUSTRYBASE)));
//	public static final RegistryObject<BlockItem> WIRE_CONNECTOR = ITEMS.register("wire_connector", () -> new BlockItem(BlockList.WIRE_CONNECTOR.get(), new Item.Properties().tab(CreativeModeTabList.INDUSTRYBASE)));

}
