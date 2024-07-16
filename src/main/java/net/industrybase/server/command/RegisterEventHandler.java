package net.industrybase.server.command;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

@EventBusSubscriber
public class RegisterEventHandler {
	@SubscribeEvent
	public static void onCommandsRegister(RegisterCommandsEvent event) {
		IndustryBaseCommand.register(event.getDispatcher());
	}
}
