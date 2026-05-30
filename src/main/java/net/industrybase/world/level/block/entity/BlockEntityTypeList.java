package net.industrybase.world.level.block.entity;

import java.util.Arrays;

import net.industrybase.api.IndustryBaseApi;
import net.industrybase.world.level.block.BlockList;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class BlockEntityTypeList {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPE = DeferredRegister
            .create(Registries.BLOCK_ENTITY_TYPE, IndustryBaseApi.MODID);

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<DynamoBlockEntity>> DYNAMO = register(
            "dynamo", DynamoBlockEntity::new, BlockList.DYNAMO);
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<CreativeDynamoBlockEntity>> CREATIVE_DYNAMO = register(
            "creative_dynamo", CreativeDynamoBlockEntity::new, BlockList.CREATIVE_DYNAMO);
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<TransmissionRodBlockEntity>> TRANSMISSION_ROD = register(
            "transmission_rod", TransmissionRodBlockEntity::new,
            BlockList.OAK_TRANSMISSION_ROD,
            BlockList.CHERRY_TRANSMISSION_ROD,
            BlockList.CRIMSON_TRANSMISSION_ROD,
            BlockList.WARPED_TRANSMISSION_ROD,
            BlockList.SPRUCE_TRANSMISSION_ROD,
            BlockList.BIRCH_TRANSMISSION_ROD,
            BlockList.JUNGLE_TRANSMISSION_ROD,
            BlockList.ACACIA_TRANSMISSION_ROD,
            BlockList.DARK_OAK_TRANSMISSION_ROD,
            BlockList.MANGROVE_TRANSMISSION_ROD,
            BlockList.STONE_TRANSMISSION_ROD,
            BlockList.IRON_TRANSMISSION_ROD,
            BlockList.GOLD_TRANSMISSION_ROD,
            BlockList.DIAMOND_TRANSMISSION_ROD);
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<SteamEngineBlockEntity>> STEAM_ENGINE = register(
            "steam_engine", SteamEngineBlockEntity::new, BlockList.STEAM_ENGINE);
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<CreativeSteamEngineBlockEntity>> CREATIVE_STEAM_ENGINE = register(
            "creative_steam_engine", CreativeSteamEngineBlockEntity::new, BlockList.CREATIVE_STEAM_ENGINE);
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<AxisConnectorBlockEntity>> AXIS_CONNECTOR = register(
            "gear_box", AxisConnectorBlockEntity::new, BlockList.AXIS_CONNECTOR);
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<WireBlockEntity>> WIRE = register("wire",
            WireBlockEntity::new, BlockList.WIRE);
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<WireConnectorBlockEntity>> WIRE_CONNECTOR = register(
            "wire_connector", WireConnectorBlockEntity::new, BlockList.WIRE_CONNECTOR);
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ElectricMotorBlockEntity>> ELECTRIC_MOTOR = register(
            "electric_motor", ElectricMotorBlockEntity::new, BlockList.ELECTRIC_MOTOR);
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<CreativeElectricMotorBlockEntity>> CREATIVE_ELECTRIC_MOTOR = register(
            "creative_electric_motor", CreativeElectricMotorBlockEntity::new, BlockList.CREATIVE_ELECTRIC_MOTOR);
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<IronPipeBlockEntity>> IRON_PIPE = register(
            "iron_pipe", IronPipeBlockEntity::new, BlockList.IRON_PIPE);
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<WaterPumpBlockEntity>> WATER_PUMP = register(
            "water_pump", WaterPumpBlockEntity::new, BlockList.WATER_PUMP);
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<FluidTankBlockEntity>> FLUID_TANK = register(
            "fluid_tank", FluidTankBlockEntity::new, BlockList.FLUID_TANK);
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<InsulatorBlockEntity>> INSULATOR = register(
            "insulator", InsulatorBlockEntity::new, BlockList.INSULATOR);

    private BlockEntityTypeList() {
    }

    @SafeVarargs
    private static <T extends BlockEntity> DeferredHolder<BlockEntityType<?>, BlockEntityType<T>> register(String name,
            BlockEntityType.BlockEntitySupplier<T> blockEntity, DeferredHolder<Block, ? extends Block>... blocks) {
        return BLOCK_ENTITY_TYPE.register(name, () -> {
            var blockList = Arrays.stream(blocks).map((holder) -> holder.get()).toArray(Block[]::new);
            return new BlockEntityType<>(blockEntity, blockList);
        });
    }
}
