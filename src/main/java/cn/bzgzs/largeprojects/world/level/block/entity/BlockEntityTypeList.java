package cn.bzgzs.largeprojects.world.level.block.entity;

import cn.bzgzs.largeprojects.LargeProjects;
import cn.bzgzs.largeprojects.world.level.block.BlockList;
import com.mojang.datafixers.DSL;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class BlockEntityTypeList {
	public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, LargeProjects.MODID);

	public static final RegistryObject<BlockEntityType<DynamoBlockEntity>> DYNAMO = BLOCK_ENTITY_TYPES.register("dynamo", () -> BlockEntityType.Builder.of(DynamoBlockEntity::new, BlockList.DYNAMO.get()).build(DSL.remainderType()));
}
