package net.industrybase.world.item;

import net.industrybase.api.IndustryBaseApi;
import net.industrybase.world.level.block.BlockList;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ItemList {
	public static final DeferredRegister.Items ITEM = DeferredRegister.createItems(IndustryBaseApi.MODID);

	public static final DeferredItem<WireCoilItem> WIRE_COIL = ITEM.register("wire_coil", WireCoilItem::new);

	public static final DeferredItem<BlockItem> DYNAMO = ITEM.register("dynamo", () -> new BlockItem(BlockList.DYNAMO.get(), new Item.Properties()));
	public static final DeferredItem<BlockItem> CREATIVE_DYNAMO = ITEM.register("creative_dynamo", () -> new BlockItem(BlockList.CREATIVE_DYNAMO.get(), new Item.Properties()));
	public static final DeferredItem<BlockItem> OAK_TRANSMISSION_ROD = ITEM.register("oak_transmission_rod", () -> new BlockEntityItem(BlockList.OAK_TRANSMISSION_ROD.get(), new Item.Properties()));
	public static final DeferredItem<BlockItem> SPRUCE_TRANSMISSION_ROD = ITEM.register("spruce_transmission_rod", () -> new BlockEntityItem(BlockList.SPRUCE_TRANSMISSION_ROD.get(), new Item.Properties()));
	public static final DeferredItem<BlockItem> BIRCH_TRANSMISSION_ROD = ITEM.register("birch_transmission_rod", () -> new BlockEntityItem(BlockList.BIRCH_TRANSMISSION_ROD.get(), new Item.Properties()));
	public static final DeferredItem<BlockItem> JUNGLE_TRANSMISSION_ROD = ITEM.register("jungle_transmission_rod", () -> new BlockEntityItem(BlockList.JUNGLE_TRANSMISSION_ROD.get(), new Item.Properties()));
	public static final DeferredItem<BlockItem> ACACIA_TRANSMISSION_ROD = ITEM.register("acacia_transmission_rod", () -> new BlockEntityItem(BlockList.ACACIA_TRANSMISSION_ROD.get(), new Item.Properties()));
	public static final DeferredItem<BlockItem> DARK_OAK_TRANSMISSION_ROD = ITEM.register("dark_oak_transmission_rod", () -> new BlockEntityItem(BlockList.DARK_OAK_TRANSMISSION_ROD.get(), new Item.Properties()));
	public static final DeferredItem<BlockItem> MANGROVE_TRANSMISSION_ROD = ITEM.register("mangrove_transmission_rod", () -> new BlockEntityItem(BlockList.MANGROVE_TRANSMISSION_ROD.get(), new Item.Properties()));
	public static final DeferredItem<BlockItem> STONE_TRANSMISSION_ROD = ITEM.register("stone_transmission_rod", () -> new BlockEntityItem(BlockList.STONE_TRANSMISSION_ROD.get(), new Item.Properties()));
	public static final DeferredItem<BlockItem> IRON_TRANSMISSION_ROD = ITEM.register("iron_transmission_rod", () -> new BlockEntityItem(BlockList.IRON_TRANSMISSION_ROD.get(), new Item.Properties()));
	public static final DeferredItem<BlockItem> GOLD_TRANSMISSION_ROD = ITEM.register("gold_transmission_rod", () -> new BlockEntityItem(BlockList.GOLD_TRANSMISSION_ROD.get(), new Item.Properties()));
	public static final DeferredItem<BlockItem> DIAMOND_TRANSMISSION_ROD = ITEM.register("diamond_transmission_rod", () -> new BlockEntityItem(BlockList.DIAMOND_TRANSMISSION_ROD.get(), new Item.Properties()));
	public static final DeferredItem<BlockItem> STEAM_ENGINE = ITEM.register("steam_engine", () -> new BlockEntityItem(BlockList.STEAM_ENGINE.get(), new Item.Properties()));
	public static final DeferredItem<BlockItem> CREATIVE_STEAM_ENGINE = ITEM.register("creative_steam_engine", () -> new BlockEntityItem(BlockList.CREATIVE_STEAM_ENGINE.get(), new Item.Properties()));
	public static final DeferredItem<BlockItem> AXIS_CONNECTOR = ITEM.register("axis_connector", () -> new BlockEntityItem(BlockList.AXIS_CONNECTOR.get(), new Item.Properties()));
	public static final DeferredItem<BlockItem> WIRE = ITEM.register("wire", () -> new BlockEntityItem(BlockList.WIRE.get(), new Item.Properties()));
	public static final DeferredItem<BlockItem> WIRE_CONNECTOR = ITEM.register("wire_connector", () -> new BlockEntityItem(BlockList.WIRE_CONNECTOR.get(), new Item.Properties()));
	public static final DeferredItem<BlockItem> ELECTRIC_MOTOR = ITEM.register("electric_motor", () -> new BlockEntityItem(BlockList.ELECTRIC_MOTOR.get(), new Item.Properties()));
	public static final DeferredItem<BlockItem> CREATIVE_ELECTRIC_MOTOR = ITEM.register("creative_electric_motor", () -> new BlockEntityItem(BlockList.CREATIVE_ELECTRIC_MOTOR.get(), new Item.Properties()));
	public static final DeferredItem<BlockItem> CHERRY_TRANSMISSION_ROD = ITEM.register("cherry_transmission_rod", () -> new BlockEntityItem(BlockList.CHERRY_TRANSMISSION_ROD.get(), new Item.Properties()));
	public static final DeferredItem<BlockItem> CRIMSON_TRANSMISSION_ROD = ITEM.register("crimson_transmission_rod", () -> new BlockEntityItem(BlockList.CRIMSON_TRANSMISSION_ROD.get(), new Item.Properties()));
	public static final DeferredItem<BlockItem> WARPED_TRANSMISSION_ROD = ITEM.register("warped_transmission_rod", () -> new BlockEntityItem(BlockList.WARPED_TRANSMISSION_ROD.get(), new Item.Properties()));
	public static final DeferredItem<BlockItem> IRON_PIPE = ITEM.register("iron_pipe", () -> new BlockItem(BlockList.IRON_PIPE.get(), new Item.Properties()));
	public static final DeferredItem<BlockItem> WATER_PUMP = ITEM.register("water_pump", () -> new BlockItem(BlockList.WATER_PUMP.get(), new Item.Properties()));


	private ItemList() {
	}
}
