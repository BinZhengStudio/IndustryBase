package cn.bzgzs.industrybase.world.level.block;

import cn.bzgzs.industrybase.api.IndustryBaseApi;
import cn.bzgzs.industrybase.world.level.block.entity.WarpedTransmissionRodBlockEntity;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class BlockList {
	public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, IndustryBaseApi.MODID);

	public static final RegistryObject<Block> DYNAMO = BLOCKS.register("dynamo", DynamoBlock::new);
	public static final RegistryObject<Block> OAK_TRANSMISSION_ROD = BLOCKS.register("oak_transmission_rod", OakTransmissionRodBlock::new);
	public static final RegistryObject<Block> SPRUCE_TRANSMISSION_ROD = BLOCKS.register("spruce_transmission_rod", SpruceTransmissionRodBlock::new);
	public static final RegistryObject<Block> BIRCH_TRANSMISSION_ROD = BLOCKS.register("birch_transmission_rod", BirchTransmissionRodBlock::new);
	public static final RegistryObject<Block> JUNGLE_TRANSMISSION_ROD = BLOCKS.register("jungle_transmission_rod", JungleTransmissionRodBlock::new);
	public static final RegistryObject<Block> ACACIA_TRANSMISSION_ROD = BLOCKS.register("acacia_transmission_rod", AcaciaTransmissionRodBlock::new);
	public static final RegistryObject<Block> DARK_OAK_TRANSMISSION_ROD = BLOCKS.register("dark_oak_transmission_rod", DarkOakTransmissionRodBlock::new);
	public static final RegistryObject<Block> MANGROVE_TRANSMISSION_ROD = BLOCKS.register("mangrove_transmission_rod", MangroveTransmissionRodBlock::new);
	public static final RegistryObject<Block> STONE_TRANSMISSION_ROD = BLOCKS.register("stone_transmission_rod", StoneTransmissionRodBlock::new);
	public static final RegistryObject<Block> IRON_TRANSMISSION_ROD = BLOCKS.register("iron_transmission_rod", IronTransmissionRodBlock::new);
	public static final RegistryObject<Block> GOLD_TRANSMISSION_ROD = BLOCKS.register("gold_transmission_rod", GoldTransmissionRodBlock::new);
	public static final RegistryObject<Block> DIAMOND_TRANSMISSION_ROD = BLOCKS.register("diamond_transmission_rod", DiamondTransmissionRodBlock::new);
	public static final RegistryObject<Block> STEAM_ENGINE = BLOCKS.register("steam_engine", SteamEngineBlock::new);
	public static final RegistryObject<Block> AXIS_CONNECTOR = BLOCKS.register("axis_connector", AxisConnectorBlock::new);
	public static final RegistryObject<Block> WIRE = BLOCKS.register("wire", WireBlock::new);
	public static final RegistryObject<Block> WIRE_CONNECTOR = BLOCKS.register("wire_connector", WireConnectorBlock::new);
	public static final RegistryObject<Block> ELECTRIC_MOTOR = BLOCKS.register("electric_motor", ElectricMotorBlock::new);
	public static final RegistryObject<Block> CHERRY_TRANSMISSION_ROD = BLOCKS.register("cherry_transmission_rod", CherryTransmissionRodBlock::new);
	public static final RegistryObject<Block> CRIMSON_TRANSMISSION_ROD = BLOCKS.register("crimson_transmission_rod", CrimsonTransmissionRodBlock::new);
	public static final RegistryObject<Block> WARPED_TRANSMISSION_ROD = BLOCKS.register("warped_transmission_rod", WarpedTransmissionRodBlock::new);

}
