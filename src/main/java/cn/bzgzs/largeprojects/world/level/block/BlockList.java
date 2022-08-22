package cn.bzgzs.largeprojects.world.level.block;

import cn.bzgzs.largeprojects.LargeProjects;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.Material;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class BlockList {
	public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, LargeProjects.MODID);

	public static final RegistryObject<Block> TEST = BLOCKS.register("test", () -> new Block(BlockBehaviour.Properties.of(Material.STONE)));
}
