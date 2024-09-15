package net.industrybase.world.level.block;

import net.industrybase.api.IndustryBaseApi;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

public class BlockList {
	public static final DeferredRegister.Blocks BLOCK = DeferredRegister.createBlocks(IndustryBaseApi.MODID);

	public static final DeferredBlock<DynamoBlock> DYNAMO = BLOCK.register("dynamo", DynamoBlock::new);
	public static final DeferredBlock<CreativeDynamoBlock> CREATIVE_DYNAMO = BLOCK.register("creative_dynamo", CreativeDynamoBlock::new);
	public static final DeferredBlock<OakTransmissionRodBlock> OAK_TRANSMISSION_ROD = BLOCK.register("oak_transmission_rod", OakTransmissionRodBlock::new);
	public static final DeferredBlock<SpruceTransmissionRodBlock> SPRUCE_TRANSMISSION_ROD = BLOCK.register("spruce_transmission_rod", SpruceTransmissionRodBlock::new);
	public static final DeferredBlock<BirchTransmissionRodBlock> BIRCH_TRANSMISSION_ROD = BLOCK.register("birch_transmission_rod", BirchTransmissionRodBlock::new);
	public static final DeferredBlock<JungleTransmissionRodBlock> JUNGLE_TRANSMISSION_ROD = BLOCK.register("jungle_transmission_rod", JungleTransmissionRodBlock::new);
	public static final DeferredBlock<AcaciaTransmissionRodBlock> ACACIA_TRANSMISSION_ROD = BLOCK.register("acacia_transmission_rod", AcaciaTransmissionRodBlock::new);
	public static final DeferredBlock<DarkOakTransmissionRodBlock> DARK_OAK_TRANSMISSION_ROD = BLOCK.register("dark_oak_transmission_rod", DarkOakTransmissionRodBlock::new);
	public static final DeferredBlock<MangroveTransmissionRodBlock> MANGROVE_TRANSMISSION_ROD = BLOCK.register("mangrove_transmission_rod", MangroveTransmissionRodBlock::new);
	public static final DeferredBlock<StoneTransmissionRodBlock> STONE_TRANSMISSION_ROD = BLOCK.register("stone_transmission_rod", StoneTransmissionRodBlock::new);
	public static final DeferredBlock<IronTransmissionRodBlock> IRON_TRANSMISSION_ROD = BLOCK.register("iron_transmission_rod", IronTransmissionRodBlock::new);
	public static final DeferredBlock<GoldTransmissionRodBlock> GOLD_TRANSMISSION_ROD = BLOCK.register("gold_transmission_rod", GoldTransmissionRodBlock::new);
	public static final DeferredBlock<DiamondTransmissionRodBlock> DIAMOND_TRANSMISSION_ROD = BLOCK.register("diamond_transmission_rod", DiamondTransmissionRodBlock::new);
	public static final DeferredBlock<SteamEngineBlock> STEAM_ENGINE = BLOCK.register("steam_engine", SteamEngineBlock::new);
	public static final DeferredBlock<CreativeSteamEngineBlock> CREATIVE_STEAM_ENGINE = BLOCK.register("creative_steam_engine", CreativeSteamEngineBlock::new);
	public static final DeferredBlock<AxisConnectorBlock> AXIS_CONNECTOR = BLOCK.register("axis_connector", AxisConnectorBlock::new);
	public static final DeferredBlock<WireBlock> WIRE = BLOCK.register("wire", WireBlock::new);
	public static final DeferredBlock<WireConnectorBlock> WIRE_CONNECTOR = BLOCK.register("wire_connector", WireConnectorBlock::new);
	public static final DeferredBlock<ElectricMotorBlock> ELECTRIC_MOTOR = BLOCK.register("electric_motor", ElectricMotorBlock::new);
	public static final DeferredBlock<CreativeElectricMotorBlock> CREATIVE_ELECTRIC_MOTOR = BLOCK.register("creative_electric_motor", CreativeElectricMotorBlock::new);
	public static final DeferredBlock<CherryTransmissionRodBlock> CHERRY_TRANSMISSION_ROD = BLOCK.register("cherry_transmission_rod", CherryTransmissionRodBlock::new);
	public static final DeferredBlock<CrimsonTransmissionRodBlock> CRIMSON_TRANSMISSION_ROD = BLOCK.register("crimson_transmission_rod", CrimsonTransmissionRodBlock::new);
	public static final DeferredBlock<WarpedTransmissionRodBlock> WARPED_TRANSMISSION_ROD = BLOCK.register("warped_transmission_rod", WarpedTransmissionRodBlock::new);
	public static final DeferredBlock<IronPipeBlock> IRON_PIPE = BLOCK.register("iron_pipe", IronPipeBlock::new);
	public static final DeferredBlock<WaterPumpBlock> WATER_PUMP = BLOCK.register("water_pump", WaterPumpBlock::new);
	public static final DeferredBlock<FluidTankBlock> FLUID_TANK = BLOCK.register("fluid_tank", FluidTankBlock::new);
	public static final DeferredBlock<InsulatorBlock> INSULATOR = BLOCK.register("insulator", InsulatorBlock::new);

	private BlockList() {
	}
}
