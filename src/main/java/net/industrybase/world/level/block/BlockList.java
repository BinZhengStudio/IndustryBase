package net.industrybase.world.level.block;

import net.industrybase.capability.IndustryBaseApi;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

public class BlockList {
    public static final DeferredRegister.Blocks BLOCK = DeferredRegister.createBlocks(IndustryBaseApi.MODID);

    public static final DeferredBlock<DynamoBlock> DYNAMO = BLOCK.registerBlock("dynamo", DynamoBlock::new,
            () -> BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BLOCK));
    public static final DeferredBlock<CreativeDynamoBlock> CREATIVE_DYNAMO = BLOCK.registerBlock("creative_dynamo",
            CreativeDynamoBlock::new, () -> BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BLOCK));
    public static final DeferredBlock<OakTransmissionRodBlock> OAK_TRANSMISSION_ROD = BLOCK
            .registerBlock("oak_transmission_rod", OakTransmissionRodBlock::new,
                    () -> BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_LOG));
    public static final DeferredBlock<SpruceTransmissionRodBlock> SPRUCE_TRANSMISSION_ROD = BLOCK
            .registerBlock("spruce_transmission_rod", SpruceTransmissionRodBlock::new,
                    () -> BlockBehaviour.Properties.ofFullCopy(Blocks.SPRUCE_LOG));
    public static final DeferredBlock<BirchTransmissionRodBlock> BIRCH_TRANSMISSION_ROD = BLOCK
            .registerBlock("birch_transmission_rod", BirchTransmissionRodBlock::new,
                    () -> BlockBehaviour.Properties.ofFullCopy(Blocks.BIRCH_LOG));
    public static final DeferredBlock<JungleTransmissionRodBlock> JUNGLE_TRANSMISSION_ROD = BLOCK
            .registerBlock("jungle_transmission_rod", JungleTransmissionRodBlock::new,
                    () -> BlockBehaviour.Properties.ofFullCopy(Blocks.JUNGLE_LOG));
    public static final DeferredBlock<AcaciaTransmissionRodBlock> ACACIA_TRANSMISSION_ROD = BLOCK
            .registerBlock("acacia_transmission_rod", AcaciaTransmissionRodBlock::new,
                    () -> BlockBehaviour.Properties.ofFullCopy(Blocks.ACACIA_LOG));
    public static final DeferredBlock<DarkOakTransmissionRodBlock> DARK_OAK_TRANSMISSION_ROD = BLOCK
            .registerBlock("dark_oak_transmission_rod", DarkOakTransmissionRodBlock::new,
                    () -> BlockBehaviour.Properties.ofFullCopy(Blocks.DARK_OAK_LOG));
    public static final DeferredBlock<MangroveTransmissionRodBlock> MANGROVE_TRANSMISSION_ROD = BLOCK
            .registerBlock("mangrove_transmission_rod", MangroveTransmissionRodBlock::new,
                    () -> BlockBehaviour.Properties.ofFullCopy(Blocks.MANGROVE_LOG));
    public static final DeferredBlock<StoneTransmissionRodBlock> STONE_TRANSMISSION_ROD = BLOCK
            .registerBlock("stone_transmission_rod", StoneTransmissionRodBlock::new,
                    () -> BlockBehaviour.Properties.ofFullCopy(Blocks.STONE));
    public static final DeferredBlock<IronTransmissionRodBlock> IRON_TRANSMISSION_ROD = BLOCK
            .registerBlock("iron_transmission_rod", IronTransmissionRodBlock::new,
                    () -> BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BLOCK));
    public static final DeferredBlock<GoldTransmissionRodBlock> GOLD_TRANSMISSION_ROD = BLOCK
            .registerBlock("gold_transmission_rod", GoldTransmissionRodBlock::new,
                    () -> BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BLOCK));
    public static final DeferredBlock<DiamondTransmissionRodBlock> DIAMOND_TRANSMISSION_ROD = BLOCK
            .registerBlock("diamond_transmission_rod", DiamondTransmissionRodBlock::new,
                    () -> BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BLOCK));
    public static final DeferredBlock<SteamEngineBlock> STEAM_ENGINE = BLOCK.registerBlock("steam_engine",
            SteamEngineBlock::new, () -> BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BLOCK));
    public static final DeferredBlock<CreativeSteamEngineBlock> CREATIVE_STEAM_ENGINE = BLOCK
            .registerBlock("creative_steam_engine", CreativeSteamEngineBlock::new,
                    () -> BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BLOCK));
    public static final DeferredBlock<AxisConnectorBlock> AXIS_CONNECTOR = BLOCK.registerBlock("axis_connector",
            AxisConnectorBlock::new, () -> BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BLOCK));
    public static final DeferredBlock<WireBlock> WIRE = BLOCK.registerBlock("wire", WireBlock::new);
    public static final DeferredBlock<WireConnectorBlock> WIRE_CONNECTOR = BLOCK.registerBlock("wire_connector",
            WireConnectorBlock::new, () -> BlockBehaviour.Properties.ofFullCopy(BlockList.WIRE.get()));
    public static final DeferredBlock<ElectricMotorBlock> ELECTRIC_MOTOR = BLOCK.registerBlock("electric_motor",
            ElectricMotorBlock::new, () -> BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BLOCK));
    public static final DeferredBlock<CreativeElectricMotorBlock> CREATIVE_ELECTRIC_MOTOR = BLOCK
            .registerBlock("creative_electric_motor", CreativeElectricMotorBlock::new,
                    () -> BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BLOCK));
    public static final DeferredBlock<CherryTransmissionRodBlock> CHERRY_TRANSMISSION_ROD = BLOCK
            .registerBlock("cherry_transmission_rod", CherryTransmissionRodBlock::new,
                    () -> BlockBehaviour.Properties.ofFullCopy(Blocks.CHERRY_LOG));
    public static final DeferredBlock<CrimsonTransmissionRodBlock> CRIMSON_TRANSMISSION_ROD = BLOCK
            .registerBlock("crimson_transmission_rod", CrimsonTransmissionRodBlock::new,
                    () -> BlockBehaviour.Properties.ofFullCopy(Blocks.CRIMSON_STEM));
    public static final DeferredBlock<WarpedTransmissionRodBlock> WARPED_TRANSMISSION_ROD = BLOCK
            .registerBlock("warped_transmission_rod", WarpedTransmissionRodBlock::new,
                    () -> BlockBehaviour.Properties.ofFullCopy(Blocks.WARPED_STEM));
    public static final DeferredBlock<IronPipeBlock> IRON_PIPE = BLOCK.registerBlock("iron_pipe", IronPipeBlock::new,
            () -> BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BLOCK));
    public static final DeferredBlock<WaterPumpBlock> WATER_PUMP = BLOCK.registerBlock("water_pump",
            WaterPumpBlock::new, () -> BlockBehaviour.Properties.ofFullCopy(BlockList.DYNAMO.get()));
    public static final DeferredBlock<FluidTankBlock> FLUID_TANK = BLOCK.registerBlock("fluid_tank",
            FluidTankBlock::new, () -> BlockBehaviour.Properties.ofFullCopy(Blocks.GLASS));
    public static final DeferredBlock<InsulatorBlock> INSULATOR = BLOCK.registerBlock("insulator", InsulatorBlock::new,
            () -> BlockBehaviour.Properties.ofFullCopy(BlockList.WIRE_CONNECTOR.get()));
}
