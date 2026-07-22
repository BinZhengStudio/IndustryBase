package net.industrybase.world.item;

import net.industrybase.capability.IndustryBaseApi;
import net.industrybase.world.level.block.BlockList;
import net.minecraft.world.item.BlockItem;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ItemList {
    public static final DeferredRegister.Items ITEM = DeferredRegister.createItems(IndustryBaseApi.MODID);

    public static final DeferredItem<WireCoilItem> WIRE_COIL = ITEM.registerItem("wire_coil", WireCoilItem::new);

    public static final DeferredItem<BlockItem> DYNAMO = ITEM.registerSimpleBlockItem(BlockList.DYNAMO);
    public static final DeferredItem<BlockItem> CREATIVE_DYNAMO = ITEM
            .registerSimpleBlockItem(BlockList.CREATIVE_DYNAMO);
    public static final DeferredItem<BlockItem> OAK_TRANSMISSION_ROD = ITEM
            .registerSimpleBlockItem(BlockList.OAK_TRANSMISSION_ROD);
    public static final DeferredItem<BlockItem> SPRUCE_TRANSMISSION_ROD = ITEM
            .registerSimpleBlockItem(BlockList.SPRUCE_TRANSMISSION_ROD);
    public static final DeferredItem<BlockItem> BIRCH_TRANSMISSION_ROD = ITEM
            .registerSimpleBlockItem(BlockList.BIRCH_TRANSMISSION_ROD);
    public static final DeferredItem<BlockItem> JUNGLE_TRANSMISSION_ROD = ITEM
            .registerSimpleBlockItem(BlockList.JUNGLE_TRANSMISSION_ROD);
    public static final DeferredItem<BlockItem> ACACIA_TRANSMISSION_ROD = ITEM
            .registerSimpleBlockItem(BlockList.ACACIA_TRANSMISSION_ROD);
    public static final DeferredItem<BlockItem> DARK_OAK_TRANSMISSION_ROD = ITEM
            .registerSimpleBlockItem(BlockList.DARK_OAK_TRANSMISSION_ROD);
    public static final DeferredItem<BlockItem> MANGROVE_TRANSMISSION_ROD = ITEM
            .registerSimpleBlockItem(BlockList.MANGROVE_TRANSMISSION_ROD);
    public static final DeferredItem<BlockItem> STONE_TRANSMISSION_ROD = ITEM
            .registerSimpleBlockItem(BlockList.STONE_TRANSMISSION_ROD);
    public static final DeferredItem<BlockItem> IRON_TRANSMISSION_ROD = ITEM
            .registerSimpleBlockItem(BlockList.IRON_TRANSMISSION_ROD);
    public static final DeferredItem<BlockItem> GOLD_TRANSMISSION_ROD = ITEM
            .registerSimpleBlockItem(BlockList.GOLD_TRANSMISSION_ROD);
    public static final DeferredItem<BlockItem> DIAMOND_TRANSMISSION_ROD = ITEM
            .registerSimpleBlockItem(BlockList.DIAMOND_TRANSMISSION_ROD);
    public static final DeferredItem<BlockItem> STEAM_ENGINE = ITEM.registerSimpleBlockItem(BlockList.STEAM_ENGINE);
    public static final DeferredItem<BlockItem> CREATIVE_STEAM_ENGINE = ITEM
            .registerSimpleBlockItem(BlockList.CREATIVE_STEAM_ENGINE);
    public static final DeferredItem<BlockItem> AXIS_CONNECTOR = ITEM.registerSimpleBlockItem(BlockList.AXIS_CONNECTOR);
    public static final DeferredItem<BlockItem> WIRE = ITEM.registerSimpleBlockItem(BlockList.WIRE);
    public static final DeferredItem<BlockItem> WIRE_CONNECTOR = ITEM.registerSimpleBlockItem(BlockList.WIRE_CONNECTOR);
    public static final DeferredItem<BlockItem> ELECTRIC_MOTOR = ITEM.registerSimpleBlockItem(BlockList.ELECTRIC_MOTOR);
    public static final DeferredItem<BlockItem> CREATIVE_ELECTRIC_MOTOR = ITEM
            .registerSimpleBlockItem(BlockList.CREATIVE_ELECTRIC_MOTOR);
    public static final DeferredItem<BlockItem> CHERRY_TRANSMISSION_ROD = ITEM
            .registerSimpleBlockItem(BlockList.CHERRY_TRANSMISSION_ROD);
    public static final DeferredItem<BlockItem> CRIMSON_TRANSMISSION_ROD = ITEM
            .registerSimpleBlockItem(BlockList.CRIMSON_TRANSMISSION_ROD);
    public static final DeferredItem<BlockItem> WARPED_TRANSMISSION_ROD = ITEM
            .registerSimpleBlockItem(BlockList.WARPED_TRANSMISSION_ROD);
    public static final DeferredItem<BlockItem> IRON_PIPE = ITEM.registerSimpleBlockItem(BlockList.IRON_PIPE);
    public static final DeferredItem<BlockItem> WATER_PUMP = ITEM.registerSimpleBlockItem(BlockList.WATER_PUMP);
    public static final DeferredItem<BlockItem> FLUID_TANK = ITEM.registerSimpleBlockItem(BlockList.FLUID_TANK);
    public static final DeferredItem<BlockItem> INSULATOR = ITEM.registerSimpleBlockItem(BlockList.INSULATOR);
}
