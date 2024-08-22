package net.industrybase.event.handler;

import net.industrybase.api.CapabilityList;
import net.industrybase.api.IndustryBaseApi;
import net.industrybase.api.transmit.TransmissionRodBlockEntity;
import net.industrybase.world.level.block.entity.*;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.registries.DeferredHolder;

@EventBusSubscriber(modid = IndustryBaseApi.MODID, bus = EventBusSubscriber.Bus.MOD)
public class BlockCapabilityRegister {
	@SubscribeEvent
	private static void registerCapabilities(final RegisterCapabilitiesEvent event) {
		// transmission rods
		registerTransmissionRod(event, BlockEntityTypeList.OAK_TRANSMISSION_ROD);
		registerTransmissionRod(event, BlockEntityTypeList.DARK_OAK_TRANSMISSION_ROD);
		registerTransmissionRod(event, BlockEntityTypeList.ACACIA_TRANSMISSION_ROD);
		registerTransmissionRod(event, BlockEntityTypeList.BIRCH_TRANSMISSION_ROD);
		registerTransmissionRod(event, BlockEntityTypeList.JUNGLE_TRANSMISSION_ROD);
		registerTransmissionRod(event, BlockEntityTypeList.SPRUCE_TRANSMISSION_ROD);
		registerTransmissionRod(event, BlockEntityTypeList.MANGROVE_TRANSMISSION_ROD);
		registerTransmissionRod(event, BlockEntityTypeList.CHERRY_TRANSMISSION_ROD);
		registerTransmissionRod(event, BlockEntityTypeList.CRIMSON_TRANSMISSION_ROD);
		registerTransmissionRod(event, BlockEntityTypeList.WARPED_TRANSMISSION_ROD);
		registerTransmissionRod(event, BlockEntityTypeList.STONE_TRANSMISSION_ROD);
		registerTransmissionRod(event, BlockEntityTypeList.IRON_TRANSMISSION_ROD);
		registerTransmissionRod(event, BlockEntityTypeList.GOLD_TRANSMISSION_ROD);
		registerTransmissionRod(event, BlockEntityTypeList.DIAMOND_TRANSMISSION_ROD);

		event.registerBlockEntity(CapabilityList.MECHANICAL_TRANSMIT, BlockEntityTypeList.ELECTRIC_MOTOR.get(), ElectricMotorBlockEntity::getTransmit);
		event.registerBlockEntity(CapabilityList.MECHANICAL_TRANSMIT, BlockEntityTypeList.CREATIVE_ELECTRIC_MOTOR.get(), CreativeElectricMotorBlockEntity::getTransmit);
		event.registerBlockEntity(CapabilityList.MECHANICAL_TRANSMIT, BlockEntityTypeList.DYNAMO.get(), DynamoBlockEntity::getTransmit);
		event.registerBlockEntity(CapabilityList.MECHANICAL_TRANSMIT, BlockEntityTypeList.CREATIVE_DYNAMO.get(), CreativeDynamoBlockEntity::getTransmit);
		event.registerBlockEntity(CapabilityList.MECHANICAL_TRANSMIT, BlockEntityTypeList.CREATIVE_STEAM_ENGINE.get(), CreativeSteamEngineBlockEntity::getTransmit);
		event.registerBlockEntity(CapabilityList.MECHANICAL_TRANSMIT, BlockEntityTypeList.AXIS_CONNECTOR.get(), AxisConnectorBlockEntity::getTransmit);
		event.registerBlockEntity(CapabilityList.MECHANICAL_TRANSMIT, BlockEntityTypeList.STEAM_ENGINE.get(), SteamEngineBlockEntity::getTransmit);

		event.registerBlockEntity(CapabilityList.ELECTRIC_POWER, BlockEntityTypeList.WIRE_CONNECTOR.get(), WireConnectorBlockEntity::getElectricPower);
		event.registerBlockEntity(CapabilityList.ELECTRIC_POWER, BlockEntityTypeList.WIRE.get(), WireBlockEntity::getElectricPower);
		event.registerBlockEntity(CapabilityList.ELECTRIC_POWER, BlockEntityTypeList.CREATIVE_DYNAMO.get(), CreativeDynamoBlockEntity::getElectricPower);
		event.registerBlockEntity(CapabilityList.ELECTRIC_POWER, BlockEntityTypeList.CREATIVE_ELECTRIC_MOTOR.get(), CreativeElectricMotorBlockEntity::getElectricPower);
		event.registerBlockEntity(CapabilityList.ELECTRIC_POWER, BlockEntityTypeList.DYNAMO.get(), DynamoBlockEntity::getElectricPower);
		event.registerBlockEntity(CapabilityList.ELECTRIC_POWER, BlockEntityTypeList.ELECTRIC_MOTOR.get(), ElectricMotorBlockEntity::getElectricPower);

		event.registerBlockEntity(Capabilities.EnergyStorage.BLOCK, BlockEntityTypeList.WIRE_CONNECTOR.get(), WireConnectorBlockEntity::getElectricPower);
		event.registerBlockEntity(Capabilities.EnergyStorage.BLOCK, BlockEntityTypeList.WIRE.get(), WireBlockEntity::getElectricPower);
		event.registerBlockEntity(Capabilities.EnergyStorage.BLOCK, BlockEntityTypeList.CREATIVE_DYNAMO.get(), CreativeDynamoBlockEntity::getElectricPower);
		event.registerBlockEntity(Capabilities.EnergyStorage.BLOCK, BlockEntityTypeList.CREATIVE_ELECTRIC_MOTOR.get(), CreativeElectricMotorBlockEntity::getElectricPower);
		event.registerBlockEntity(Capabilities.EnergyStorage.BLOCK, BlockEntityTypeList.DYNAMO.get(), DynamoBlockEntity::getElectricPower);
		event.registerBlockEntity(Capabilities.EnergyStorage.BLOCK, BlockEntityTypeList.ELECTRIC_MOTOR.get(), ElectricMotorBlockEntity::getElectricPower);

		event.registerBlockEntity(Capabilities.FluidHandler.BLOCK, BlockEntityTypeList.STEAM_ENGINE.get(), SteamEngineBlockEntity::getTank);
		event.registerBlockEntity(Capabilities.FluidHandler.BLOCK, BlockEntityTypeList.FLUID_TANK.get(), FluidTankBlockEntity::getTank);
	}

	private static <T extends TransmissionRodBlockEntity> void registerTransmissionRod(RegisterCapabilitiesEvent event,
																					   DeferredHolder<BlockEntityType<?>, BlockEntityType<T>> holder) {
		event.registerBlockEntity(
				CapabilityList.MECHANICAL_TRANSMIT,
				holder.get(),
				TransmissionRodBlockEntity::getTransmit);
	}
}
