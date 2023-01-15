package cn.bzgzs.industrybase.server.command;

import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber
public class RegisterEventHandler {
	@SubscribeEvent
	public static void onCommandsRegister(RegisterCommandsEvent event) {
		IndustryBaseCommand.register(event.getDispatcher());
	}
}
