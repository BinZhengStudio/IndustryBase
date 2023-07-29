package cn.bzgzs.industrybase.world.level.block.entity;

import cn.bzgzs.industrybase.api.IndustryBaseApi;
import cn.bzgzs.industrybase.api.transmit.TransmissionRodBlockEntity;
import cn.bzgzs.industrybase.world.level.block.BlockList;
import com.mojang.datafixers.DSL;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class BlockEntityTypeList {
	public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, IndustryBaseApi.MODID);

	public static final RegistryObject<BlockEntityType<DynamoBlockEntity>> DYNAMO = register("dynamo", DynamoBlockEntity::new, BlockList.DYNAMO);
	public static final RegistryObject<BlockEntityType<CreativeDynamoBlockEntity>> CREATIVE_DYNAMO = register("creative_dynamo", CreativeDynamoBlockEntity::new, BlockList.CREATIVE_DYNAMO);
	public static final RegistryObject<BlockEntityType<OakTransmissionRodBlockEntity>> OAK_TRANSMISSION_ROD = transmissionRod("oak", OakTransmissionRodBlockEntity::new, BlockList.OAK_TRANSMISSION_ROD);
	public static final RegistryObject<BlockEntityType<CherryTransmissionRodBlockEntity>> CHERRY_TRANSMISSION_ROD = transmissionRod("cherry_transmission_rod", CherryTransmissionRodBlockEntity::new, BlockList.CHERRY_TRANSMISSION_ROD);
	public static final RegistryObject<BlockEntityType<CrimsonTransmissionRodBlockEntity>> CRIMSON_TRANSMISSION_ROD = transmissionRod("crimson_transmission_rod", CrimsonTransmissionRodBlockEntity::new, BlockList.CRIMSON_TRANSMISSION_ROD);
	public static final RegistryObject<BlockEntityType<WarpedTransmissionRodBlockEntity>> WARPED_TRANSMISSION_ROD = transmissionRod("warped_transmission_rod", WarpedTransmissionRodBlockEntity::new, BlockList.WARPED_TRANSMISSION_ROD);
	public static final RegistryObject<BlockEntityType<SpruceTransmissionRodBlockEntity>> SPRUCE_TRANSMISSION_ROD = transmissionRod("spruce", SpruceTransmissionRodBlockEntity::new, BlockList.SPRUCE_TRANSMISSION_ROD);
	public static final RegistryObject<BlockEntityType<BirchTransmissionRodBlockEntity>> BIRCH_TRANSMISSION_ROD = transmissionRod("birch", BirchTransmissionRodBlockEntity::new, BlockList.BIRCH_TRANSMISSION_ROD);
	public static final RegistryObject<BlockEntityType<JungleTransmissionRodBlockEntity>> JUNGLE_TRANSMISSION_ROD = transmissionRod("jungle", JungleTransmissionRodBlockEntity::new, BlockList.JUNGLE_TRANSMISSION_ROD);
	public static final RegistryObject<BlockEntityType<AcaciaTransmissionRodBlockEntity>> ACACIA_TRANSMISSION_ROD = transmissionRod("acacia", AcaciaTransmissionRodBlockEntity::new, BlockList.ACACIA_TRANSMISSION_ROD);
	public static final RegistryObject<BlockEntityType<DarkOakTransmissionRodBlockEntity>> DARK_OAK_TRANSMISSION_ROD = transmissionRod("dark_oak", DarkOakTransmissionRodBlockEntity::new, BlockList.DARK_OAK_TRANSMISSION_ROD);
	public static final RegistryObject<BlockEntityType<MangroveTransmissionRodBlockEntity>> MANGROVE_TRANSMISSION_ROD = transmissionRod("mangrove", MangroveTransmissionRodBlockEntity::new, BlockList.MANGROVE_TRANSMISSION_ROD);
	public static final RegistryObject<BlockEntityType<StoneTransmissionRodBlockEntity>> STONE_TRANSMISSION_ROD = transmissionRod("stone", StoneTransmissionRodBlockEntity::new, BlockList.STONE_TRANSMISSION_ROD);
	public static final RegistryObject<BlockEntityType<IronTransmissionRodBlockEntity>> IRON_TRANSMISSION_ROD = transmissionRod("iron", IronTransmissionRodBlockEntity::new, BlockList.IRON_TRANSMISSION_ROD);
	public static final RegistryObject<BlockEntityType<GoldTransmissionRodBlockEntity>> GOLD_TRANSMISSION_ROD = transmissionRod("gold", GoldTransmissionRodBlockEntity::new, BlockList.GOLD_TRANSMISSION_ROD);
	public static final RegistryObject<BlockEntityType<DiamondTransmissionRodBlockEntity>> DIAMOND_TRANSMISSION_ROD = transmissionRod("diamond", DiamondTransmissionRodBlockEntity::new, BlockList.DIAMOND_TRANSMISSION_ROD);
	public static final RegistryObject<BlockEntityType<SteamEngineBlockEntity>> STEAM_ENGINE = register("steam_engine", SteamEngineBlockEntity::new, BlockList.STEAM_ENGINE);
	public static final RegistryObject<BlockEntityType<CreativeSteamEngineBlockEntity>> CREATIVE_STEAM_ENGINE = register("creative_steam_engine", CreativeSteamEngineBlockEntity::new, BlockList.CREATIVE_STEAM_ENGINE);
	public static final RegistryObject<BlockEntityType<AxisConnectorBlockEntity>> GEAR_BOX = register("gear_box", AxisConnectorBlockEntity::new, BlockList.AXIS_CONNECTOR);
	public static final RegistryObject<BlockEntityType<WireBlockEntity>> WIRE = register("wire", WireBlockEntity::new, BlockList.WIRE);
	public static final RegistryObject<BlockEntityType<WireConnectorBlockEntity>> WIRE_CONNECTOR = register("wire_connector", WireConnectorBlockEntity::new, BlockList.WIRE_CONNECTOR);
	public static final RegistryObject<BlockEntityType<ElectricMotorBlockEntity>> ELECTRIC_MOTOR = register("electric_motor", ElectricMotorBlockEntity::new, BlockList.ELECTRIC_MOTOR);
	public static final RegistryObject<BlockEntityType<CreativeElectricMotorBlockEntity>> CREATIVE_ELECTRIC_MOTOR = register("creative_electric_motor", CreativeElectricMotorBlockEntity::new, BlockList.CREATIVE_ELECTRIC_MOTOR);

	private static <T extends BlockEntity> RegistryObject<BlockEntityType<T>> register(String name, BlockEntityType.BlockEntitySupplier<T> blockEntity, RegistryObject<? extends Block> block) {
		return BLOCK_ENTITY_TYPES.register(name, () -> BlockEntityType.Builder.of(blockEntity, block.get()).build(DSL.remainderType()));
	}

	private static <T extends TransmissionRodBlockEntity> RegistryObject<BlockEntityType<T>> transmissionRod(String material, BlockEntityType.BlockEntitySupplier<T> blockEntity, RegistryObject<? extends Block> block) {
		return register(material + "_transmission_rod", blockEntity, block);
	}
}
