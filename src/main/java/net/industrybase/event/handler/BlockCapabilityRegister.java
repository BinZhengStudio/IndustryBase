package net.industrybase.event.handler;

import net.industrybase.capability.CapabilityList;
import net.industrybase.capability.IndustryBaseApi;
import net.industrybase.world.level.block.entity.*;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

@EventBusSubscriber(modid = IndustryBaseApi.MODID)
public class BlockCapabilityRegister {
	@SubscribeEvent
	private static void registerCapabilities(final RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(CapabilityList.MECHANICAL_TRANSMIT, BlockEntityTypeList.TRANSMISSION_ROD.get(), TransmissionRodBlockEntity::getTransmit);
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

		event.registerBlockEntity(Capabilities.Energy.BLOCK, BlockEntityTypeList.WIRE_CONNECTOR.get(), WireConnectorBlockEntity::getElectricPower);
		event.registerBlockEntity(Capabilities.Energy.BLOCK, BlockEntityTypeList.WIRE.get(), WireBlockEntity::getElectricPower);
		event.registerBlockEntity(Capabilities.Energy.BLOCK, BlockEntityTypeList.CREATIVE_DYNAMO.get(), CreativeDynamoBlockEntity::getElectricPower);
		event.registerBlockEntity(Capabilities.Energy.BLOCK, BlockEntityTypeList.CREATIVE_ELECTRIC_MOTOR.get(), CreativeElectricMotorBlockEntity::getElectricPower);
		event.registerBlockEntity(Capabilities.Energy.BLOCK, BlockEntityTypeList.DYNAMO.get(), DynamoBlockEntity::getElectricPower);
		event.registerBlockEntity(Capabilities.Energy.BLOCK, BlockEntityTypeList.ELECTRIC_MOTOR.get(), ElectricMotorBlockEntity::getElectricPower);

		event.registerBlockEntity(Capabilities.Fluid.BLOCK, BlockEntityTypeList.STEAM_ENGINE.get(), SteamEngineBlockEntity::getTank);
		event.registerBlockEntity(Capabilities.Fluid.BLOCK, BlockEntityTypeList.CREATIVE_STEAM_ENGINE.get(), CreativeSteamEngineBlockEntity::getTank);
		event.registerBlockEntity(Capabilities.Fluid.BLOCK, BlockEntityTypeList.FLUID_TANK.get(), FluidTankBlockEntity::getTank);
	}
}
