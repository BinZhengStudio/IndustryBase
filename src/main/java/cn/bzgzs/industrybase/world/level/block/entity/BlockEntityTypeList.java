package cn.bzgzs.industrybase.world.level.block.entity;

import cn.bzgzs.industrybase.api.Preference;
import cn.bzgzs.industrybase.world.level.block.BlockList;
import com.mojang.datafixers.DSL;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class BlockEntityTypeList {
	public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, Preference.MODID);

	public static final RegistryObject<BlockEntityType<DynamoBlockEntity>> DYNAMO = BLOCK_ENTITY_TYPES.register("dynamo", () -> BlockEntityType.Builder.of(DynamoBlockEntity::new, BlockList.DYNAMO.get()).build(DSL.remainderType()));
	public static final RegistryObject<BlockEntityType<IronTransmissionRodBlockEntity>> IRON_TRANSMISSION_ROD = BLOCK_ENTITY_TYPES.register("iron_transmission_rod", () -> BlockEntityType.Builder.of(IronTransmissionRodBlockEntity::new, BlockList.IRON_TRANSMISSION_ROD.get()).build(DSL.remainderType()));
	public static final RegistryObject<BlockEntityType<GoldTransmissionRodBlockEntity>> GOLD_TRANSMISSION_ROD = BLOCK_ENTITY_TYPES.register("gold_transmission_rod", () -> BlockEntityType.Builder.of(GoldTransmissionRodBlockEntity::new, BlockList.GOLD_TRANSMISSION_ROD.get()).build(DSL.remainderType()));
	public static final RegistryObject<BlockEntityType<SteamEngineBlockEntity>> STEAM_ENGINE = BLOCK_ENTITY_TYPES.register("steam_engine", () -> BlockEntityType.Builder.of(SteamEngineBlockEntity::new, BlockList.STEAM_ENGINE.get()).build(DSL.remainderType()));
	public static final RegistryObject<BlockEntityType<AxisConnectorBlockEntity>> GEAR_BOX = BLOCK_ENTITY_TYPES.register("gear_box", () -> BlockEntityType.Builder.of(AxisConnectorBlockEntity::new, BlockList.AXIS_CONNECTOR.get()).build(DSL.remainderType()));
	public static final RegistryObject<BlockEntityType<WireBlockEntity>> WIRE = BLOCK_ENTITY_TYPES.register("wire", () -> BlockEntityType.Builder.of(WireBlockEntity::new, BlockList.WIRE.get()).build(DSL.remainderType()));
	public static final RegistryObject<BlockEntityType<WireConnectorBlockEntity>> WIRE_CONNECTOR = BLOCK_ENTITY_TYPES.register("wire_connector", () -> BlockEntityType.Builder.of(WireConnectorBlockEntity::new, BlockList.WIRE_CONNECTOR.get()).build(DSL.remainderType()));
	public static final RegistryObject<BlockEntityType<ElectricMotorBlockEntity>> ELECTRIC_MOTOR = BLOCK_ENTITY_TYPES.register("electric_motor", () -> BlockEntityType.Builder.of(ElectricMotorBlockEntity::new, BlockList.ELECTRIC_MOTOR.get()).build(DSL.remainderType()));

}
