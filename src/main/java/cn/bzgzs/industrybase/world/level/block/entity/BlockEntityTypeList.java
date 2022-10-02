package cn.bzgzs.industrybase.world.level.block.entity;

import cn.bzgzs.industrybase.IndustryBase;
import cn.bzgzs.industrybase.world.level.block.BlockList;
import com.mojang.datafixers.DSL;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class BlockEntityTypeList {
	public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, IndustryBase.MODID);

	public static final RegistryObject<BlockEntityType<DynamoBlockEntity>> DYNAMO = BLOCK_ENTITY_TYPES.register("dynamo", () -> BlockEntityType.Builder.of(DynamoBlockEntity::new, BlockList.DYNAMO.get()).build(DSL.remainderType()));
	public static final RegistryObject<BlockEntityType<TransmissionRodBlockEntity>> TRANSMISSION_ROD = BLOCK_ENTITY_TYPES.register("transmission_rod", () -> BlockEntityType.Builder.of(TransmissionRodBlockEntity::new, BlockList.TRANSMISSION_ROD.get()).build(DSL.remainderType()));
	public static final RegistryObject<BlockEntityType<SteamEngineBlockEntity>> STEAM_ENGINE = BLOCK_ENTITY_TYPES.register("steam_engine", () -> BlockEntityType.Builder.of(SteamEngineBlockEntity::new, BlockList.STEAM_ENGINE.get()).build(DSL.remainderType()));
	public static final RegistryObject<BlockEntityType<GearBoxBlockEntity>> GEAR_BOX = BLOCK_ENTITY_TYPES.register("gear_box", () -> BlockEntityType.Builder.of(GearBoxBlockEntity::new, BlockList.GEAR_BOX.get()).build(DSL.remainderType()));
	public static final RegistryObject<BlockEntityType<WireBlockEntity>> WIRE = BLOCK_ENTITY_TYPES.register("wire", () -> BlockEntityType.Builder.of(WireBlockEntity::new, BlockList.WIRE.get()).build(DSL.remainderType()));
}
