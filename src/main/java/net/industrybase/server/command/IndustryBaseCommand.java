package net.industrybase.server.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

public class IndustryBaseCommand {
	public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
		LiteralCommandNode<CommandSourceStack> industrybase = dispatcher.register(
				Commands.literal("industrybase")
						.then(WireCommand.register())
		);
		dispatcher.register(
				Commands.literal("ib")
						.redirect(industrybase));
	}
}
