package cn.bzgzs.largeprojects.world.level.block;

import cn.bzgzs.largeprojects.LargeProjects;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class BlockList {
	public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, LargeProjects.MODID);

	public static final RegistryObject<Block> DYNAMO = BLOCKS.register("dynamo", DynamoBlock::new);
	public static final RegistryObject<Block> TRANSMISSION_ROD = BLOCKS.register("transmission_rod", TransmissionRodBlock::new);
}
