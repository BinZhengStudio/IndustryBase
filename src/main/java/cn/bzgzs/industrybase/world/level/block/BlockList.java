package cn.bzgzs.industrybase.world.level.block;

import cn.bzgzs.industrybase.api.Preference;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class BlockList {
	public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, Preference.MODID);

	public static final RegistryObject<Block> DYNAMO = BLOCKS.register("dynamo", DynamoBlock::new);
	public static final RegistryObject<Block> IRON_TRANSMISSION_ROD = BLOCKS.register("iron_transmission_rod", IronTransmissionRodBlock::new);
	public static final RegistryObject<Block> GOLD_TRANSMISSION_ROD = BLOCKS.register("gold_transmission_rod", GoldTransmissionRodBlock::new);
	public static final RegistryObject<Block> STEAM_ENGINE = BLOCKS.register("steam_engine", SteamEngineBlock::new);
	public static final RegistryObject<Block> AXIS_CONNECTOR = BLOCKS.register("axis_connector", AxisConnectorBlock::new);
	public static final RegistryObject<Block> WIRE = BLOCKS.register("wire", WireBlock::new);
	public static final RegistryObject<Block> WIRE_CONNECTOR = BLOCKS.register("wire_connector", WireConnectorBlock::new);

}
