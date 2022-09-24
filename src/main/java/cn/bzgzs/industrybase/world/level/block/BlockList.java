package cn.bzgzs.industrybase.world.level.block;

import cn.bzgzs.industrybase.IndustryBase;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class BlockList {
	public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, IndustryBase.MODID);

	public static final RegistryObject<Block> DYNAMO = BLOCKS.register("dynamo", DynamoBlock::new);
	public static final RegistryObject<Block> TRANSMISSION_ROD = BLOCKS.register("transmission_rod", TransmissionRodBlock::new);
	public static final RegistryObject<Block> STEAM_ENGINE = BLOCKS.register("steam_engine", SteamEngineBlock::new);
	public static final RegistryObject<Block> GEAR_BOX = BLOCKS.register("gear_box", GearBoxBlock::new);
}
