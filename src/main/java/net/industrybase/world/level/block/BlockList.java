package net.industrybase.world.level.block;

import net.industrybase.api.IndustryBaseApi;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

public class BlockList {
	public static final DeferredRegister.Blocks BLOCK = DeferredRegister.createBlocks(IndustryBaseApi.MODID);

	public static final DeferredBlock<Block> DYNAMO = BLOCK.register("dynamo", DynamoBlock::new);
	public static final DeferredBlock<Block> CREATIVE_DYNAMO = BLOCK.register("creative_dynamo", CreativeDynamoBlock::new);
	public static final DeferredBlock<Block> OAK_TRANSMISSION_ROD = BLOCK.register("oak_transmission_rod", OakTransmissionRodBlock::new);
	public static final DeferredBlock<Block> SPRUCE_TRANSMISSION_ROD = BLOCK.register("spruce_transmission_rod", SpruceTransmissionRodBlock::new);
	public static final DeferredBlock<Block> BIRCH_TRANSMISSION_ROD = BLOCK.register("birch_transmission_rod", BirchTransmissionRodBlock::new);
	public static final DeferredBlock<Block> JUNGLE_TRANSMISSION_ROD = BLOCK.register("jungle_transmission_rod", JungleTransmissionRodBlock::new);
	public static final DeferredBlock<Block> ACACIA_TRANSMISSION_ROD = BLOCK.register("acacia_transmission_rod", AcaciaTransmissionRodBlock::new);
	public static final DeferredBlock<Block> DARK_OAK_TRANSMISSION_ROD = BLOCK.register("dark_oak_transmission_rod", DarkOakTransmissionRodBlock::new);
	public static final DeferredBlock<Block> MANGROVE_TRANSMISSION_ROD = BLOCK.register("mangrove_transmission_rod", MangroveTransmissionRodBlock::new);
	public static final DeferredBlock<Block> STONE_TRANSMISSION_ROD = BLOCK.register("stone_transmission_rod", StoneTransmissionRodBlock::new);
	public static final DeferredBlock<Block> IRON_TRANSMISSION_ROD = BLOCK.register("iron_transmission_rod", IronTransmissionRodBlock::new);
	public static final DeferredBlock<Block> GOLD_TRANSMISSION_ROD = BLOCK.register("gold_transmission_rod", GoldTransmissionRodBlock::new);
	public static final DeferredBlock<Block> DIAMOND_TRANSMISSION_ROD = BLOCK.register("diamond_transmission_rod", DiamondTransmissionRodBlock::new);
	public static final DeferredBlock<Block> STEAM_ENGINE = BLOCK.register("steam_engine", SteamEngineBlock::new);
	public static final DeferredBlock<Block> CREATIVE_STEAM_ENGINE = BLOCK.register("creative_steam_engine", CreativeSteamEngineBlock::new);
	public static final DeferredBlock<Block> AXIS_CONNECTOR = BLOCK.register("axis_connector", AxisConnectorBlock::new);
	public static final DeferredBlock<Block> WIRE = BLOCK.register("wire", WireBlock::new);
	public static final DeferredBlock<Block> WIRE_CONNECTOR = BLOCK.register("wire_connector", WireConnectorBlock::new);
	public static final DeferredBlock<Block> ELECTRIC_MOTOR = BLOCK.register("electric_motor", ElectricMotorBlock::new);
	public static final DeferredBlock<Block> CREATIVE_ELECTRIC_MOTOR = BLOCK.register("creative_electric_motor", CreativeElectricMotorBlock::new);
	public static final DeferredBlock<Block> CHERRY_TRANSMISSION_ROD = BLOCK.register("cherry_transmission_rod", CherryTransmissionRodBlock::new);
	public static final DeferredBlock<Block> CRIMSON_TRANSMISSION_ROD = BLOCK.register("crimson_transmission_rod", CrimsonTransmissionRodBlock::new);
	public static final DeferredBlock<Block> WARPED_TRANSMISSION_ROD = BLOCK.register("warped_transmission_rod", WarpedTransmissionRodBlock::new);
	public static final DeferredBlock<Block> IRON_PIPE = BLOCK.register("iron_pipe", IronPipeBlock::new);
	public static final DeferredBlock<Block> WATER_PUMP = BLOCK.register("water_pump", WaterPumpBlock::new);

	private BlockList() {
	}
}
