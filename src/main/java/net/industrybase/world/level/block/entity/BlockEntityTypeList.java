package net.industrybase.world.level.block.entity;

import com.mojang.datafixers.DSL;
import net.industrybase.api.IndustryBaseApi;
import net.industrybase.api.transmit.TransmissionRodBlockEntity;
import net.industrybase.world.level.block.BlockList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class BlockEntityTypeList {
	public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPE = DeferredRegister.create(BuiltInRegistries.BLOCK_ENTITY_TYPE, IndustryBaseApi.MODID);

	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<DynamoBlockEntity>> DYNAMO = register("dynamo", DynamoBlockEntity::new, BlockList.DYNAMO);
	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<CreativeDynamoBlockEntity>> CREATIVE_DYNAMO = register("creative_dynamo", CreativeDynamoBlockEntity::new, BlockList.CREATIVE_DYNAMO);
	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<OakTransmissionRodBlockEntity>> OAK_TRANSMISSION_ROD = transmissionRod("oak", OakTransmissionRodBlockEntity::new, BlockList.OAK_TRANSMISSION_ROD);
	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<CherryTransmissionRodBlockEntity>> CHERRY_TRANSMISSION_ROD = transmissionRod("cherry", CherryTransmissionRodBlockEntity::new, BlockList.CHERRY_TRANSMISSION_ROD);
	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<CrimsonTransmissionRodBlockEntity>> CRIMSON_TRANSMISSION_ROD = transmissionRod("crimson", CrimsonTransmissionRodBlockEntity::new, BlockList.CRIMSON_TRANSMISSION_ROD);
	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<WarpedTransmissionRodBlockEntity>> WARPED_TRANSMISSION_ROD = transmissionRod("warped", WarpedTransmissionRodBlockEntity::new, BlockList.WARPED_TRANSMISSION_ROD);
	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<SpruceTransmissionRodBlockEntity>> SPRUCE_TRANSMISSION_ROD = transmissionRod("spruce", SpruceTransmissionRodBlockEntity::new, BlockList.SPRUCE_TRANSMISSION_ROD);
	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<BirchTransmissionRodBlockEntity>> BIRCH_TRANSMISSION_ROD = transmissionRod("birch", BirchTransmissionRodBlockEntity::new, BlockList.BIRCH_TRANSMISSION_ROD);
	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<JungleTransmissionRodBlockEntity>> JUNGLE_TRANSMISSION_ROD = transmissionRod("jungle", JungleTransmissionRodBlockEntity::new, BlockList.JUNGLE_TRANSMISSION_ROD);
	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<AcaciaTransmissionRodBlockEntity>> ACACIA_TRANSMISSION_ROD = transmissionRod("acacia", AcaciaTransmissionRodBlockEntity::new, BlockList.ACACIA_TRANSMISSION_ROD);
	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<DarkOakTransmissionRodBlockEntity>> DARK_OAK_TRANSMISSION_ROD = transmissionRod("dark_oak", DarkOakTransmissionRodBlockEntity::new, BlockList.DARK_OAK_TRANSMISSION_ROD);
	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<MangroveTransmissionRodBlockEntity>> MANGROVE_TRANSMISSION_ROD = transmissionRod("mangrove", MangroveTransmissionRodBlockEntity::new, BlockList.MANGROVE_TRANSMISSION_ROD);
	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<StoneTransmissionRodBlockEntity>> STONE_TRANSMISSION_ROD = transmissionRod("stone", StoneTransmissionRodBlockEntity::new, BlockList.STONE_TRANSMISSION_ROD);
	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<IronTransmissionRodBlockEntity>> IRON_TRANSMISSION_ROD = transmissionRod("iron", IronTransmissionRodBlockEntity::new, BlockList.IRON_TRANSMISSION_ROD);
	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<GoldTransmissionRodBlockEntity>> GOLD_TRANSMISSION_ROD = transmissionRod("gold", GoldTransmissionRodBlockEntity::new, BlockList.GOLD_TRANSMISSION_ROD);
	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<DiamondTransmissionRodBlockEntity>> DIAMOND_TRANSMISSION_ROD = transmissionRod("diamond", DiamondTransmissionRodBlockEntity::new, BlockList.DIAMOND_TRANSMISSION_ROD);
	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<SteamEngineBlockEntity>> STEAM_ENGINE = register("steam_engine", SteamEngineBlockEntity::new, BlockList.STEAM_ENGINE);
	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<CreativeSteamEngineBlockEntity>> CREATIVE_STEAM_ENGINE = register("creative_steam_engine", CreativeSteamEngineBlockEntity::new, BlockList.CREATIVE_STEAM_ENGINE);
	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<AxisConnectorBlockEntity>> AXIS_CONNECTOR = register("gear_box", AxisConnectorBlockEntity::new, BlockList.AXIS_CONNECTOR);
	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<WireBlockEntity>> WIRE = register("wire", WireBlockEntity::new, BlockList.WIRE);
	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<WireConnectorBlockEntity>> WIRE_CONNECTOR = register("wire_connector", WireConnectorBlockEntity::new, BlockList.WIRE_CONNECTOR);
	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ElectricMotorBlockEntity>> ELECTRIC_MOTOR = register("electric_motor", ElectricMotorBlockEntity::new, BlockList.ELECTRIC_MOTOR);
	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<CreativeElectricMotorBlockEntity>> CREATIVE_ELECTRIC_MOTOR = register("creative_electric_motor", CreativeElectricMotorBlockEntity::new, BlockList.CREATIVE_ELECTRIC_MOTOR);
	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<IronPipeBlockEntity>> IRON_PIPE = register("iron_pipe", IronPipeBlockEntity::new, BlockList.IRON_PIPE);
	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<WaterPumpBlockEntity>> WATER_PUMP = register("water_pump", WaterPumpBlockEntity::new, BlockList.WATER_PUMP);

	private BlockEntityTypeList() {
	}

	private static <T extends BlockEntity> DeferredHolder<BlockEntityType<?>, BlockEntityType<T>> register(String name, BlockEntityType.BlockEntitySupplier<T> blockEntity, DeferredHolder<Block, ? extends Block> block) {
		return BLOCK_ENTITY_TYPE.register(name, () -> BlockEntityType.Builder.of(blockEntity, block.get()).build(DSL.remainderType()));
	}

	private static <T extends TransmissionRodBlockEntity> DeferredHolder<BlockEntityType<?>, BlockEntityType<T>> transmissionRod(String material, BlockEntityType.BlockEntitySupplier<T> blockEntity, DeferredHolder<Block, ? extends Block> block) {
		return register(material + "_transmission_rod", blockEntity, block);
	}
}
