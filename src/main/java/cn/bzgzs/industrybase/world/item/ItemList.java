package cn.bzgzs.industrybase.world.item;

import cn.bzgzs.industrybase.api.Preference;
import cn.bzgzs.industrybase.world.level.block.BlockList;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ItemList {
	public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, Preference.MODID);

//	public static final RegistryObject<Item> WIRE_COIL = ITEMS.register("wire_coil", WireCoilItem::new);

	public static final RegistryObject<BlockItem> DYNAMO = ITEMS.register("dynamo", () -> new BlockItem(BlockList.DYNAMO.get(), new Item.Properties().tab(CreativeModeTabList.INDUSTRYBASE)));
	public static final RegistryObject<BlockItem> OAK_TRANSMISSION_ROD = ITEMS.register("oak_transmission_rod", () -> new BlockItem(BlockList.OAK_TRANSMISSION_ROD.get(), new Item.Properties().tab(CreativeModeTabList.INDUSTRYBASE)));
	public static final RegistryObject<BlockItem> SPRUCE_TRANSMISSION_ROD = ITEMS.register("spruce_transmission_rod", () -> new BlockItem(BlockList.SPRUCE_TRANSMISSION_ROD.get(), new Item.Properties().tab(CreativeModeTabList.INDUSTRYBASE)));
	public static final RegistryObject<BlockItem> BIRCH_TRANSMISSION_ROD = ITEMS.register("birch_transmission_rod", () -> new BlockItem(BlockList.BIRCH_TRANSMISSION_ROD.get(), new Item.Properties().tab(CreativeModeTabList.INDUSTRYBASE)));
	public static final RegistryObject<BlockItem> JUNGLE_TRANSMISSION_ROD = ITEMS.register("jungle_transmission_rod", () -> new BlockItem(BlockList.JUNGLE_TRANSMISSION_ROD.get(), new Item.Properties().tab(CreativeModeTabList.INDUSTRYBASE)));
	public static final RegistryObject<BlockItem> ACACIA_TRANSMISSION_ROD = ITEMS.register("acacia_transmission_rod", () -> new BlockItem(BlockList.ACACIA_TRANSMISSION_ROD.get(), new Item.Properties().tab(CreativeModeTabList.INDUSTRYBASE)));
	public static final RegistryObject<BlockItem> DARK_OAK_TRANSMISSION_ROD = ITEMS.register("dark_oak_transmission_rod", () -> new BlockItem(BlockList.DARK_OAK_TRANSMISSION_ROD.get(), new Item.Properties().tab(CreativeModeTabList.INDUSTRYBASE)));
	public static final RegistryObject<BlockItem> MANGROVE_TRANSMISSION_ROD = ITEMS.register("mangrove_transmission_rod", () -> new BlockItem(BlockList.MANGROVE_TRANSMISSION_ROD.get(), new Item.Properties().tab(CreativeModeTabList.INDUSTRYBASE)));
	public static final RegistryObject<BlockItem> STONE_TRANSMISSION_ROD = ITEMS.register("stone_transmission_rod", () -> new BlockItem(BlockList.STONE_TRANSMISSION_ROD.get(), new Item.Properties().tab(CreativeModeTabList.INDUSTRYBASE)));
	public static final RegistryObject<BlockItem> IRON_TRANSMISSION_ROD = ITEMS.register("iron_transmission_rod", () -> new BlockItem(BlockList.IRON_TRANSMISSION_ROD.get(), new Item.Properties().tab(CreativeModeTabList.INDUSTRYBASE)));
	public static final RegistryObject<BlockItem> GOLD_TRANSMISSION_ROD = ITEMS.register("gold_transmission_rod", () -> new BlockItem(BlockList.GOLD_TRANSMISSION_ROD.get(), new Item.Properties().tab(CreativeModeTabList.INDUSTRYBASE)));
	public static final RegistryObject<BlockItem> DIAMOND_TRANSMISSION_ROD = ITEMS.register("diamond_transmission_rod", () -> new BlockItem(BlockList.DIAMOND_TRANSMISSION_ROD.get(), new Item.Properties().tab(CreativeModeTabList.INDUSTRYBASE)));
	public static final RegistryObject<BlockItem> STEAM_ENGINE = ITEMS.register("steam_engine", () -> new BlockItem(BlockList.STEAM_ENGINE.get(), new Item.Properties().tab(CreativeModeTabList.INDUSTRYBASE)));
	public static final RegistryObject<BlockItem> AXIS_CONNECTOR = ITEMS.register("axis_connector", () -> new BlockItem(BlockList.AXIS_CONNECTOR.get(), new Item.Properties().tab(CreativeModeTabList.INDUSTRYBASE)));
	public static final RegistryObject<BlockItem> WIRE = ITEMS.register("wire", () -> new BlockItem(BlockList.WIRE.get(), new Item.Properties().tab(CreativeModeTabList.INDUSTRYBASE)));
	public static final RegistryObject<BlockItem> WIRE_CONNECTOR = ITEMS.register("wire_connector", () -> new BlockItem(BlockList.WIRE_CONNECTOR.get(), new Item.Properties().tab(CreativeModeTabList.INDUSTRYBASE)));
	public static final RegistryObject<BlockItem> ELECTRIC_MOTOR = ITEMS.register("electric_motor", () -> new BlockItem(BlockList.ELECTRIC_MOTOR.get(), new Item.Properties().tab(CreativeModeTabList.INDUSTRYBASE)));

}
