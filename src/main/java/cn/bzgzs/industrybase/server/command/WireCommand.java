package cn.bzgzs.industrybase.server.command;

import cn.bzgzs.industrybase.api.IndustryBaseApi;
import cn.bzgzs.industrybase.api.electric.ConnectHelper;
import cn.bzgzs.industrybase.api.electric.IWireConnectable;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

public class WireCommand {
	private static final SimpleCommandExceptionType ERROR_NOT_CONNECTABLE = new SimpleCommandExceptionType(Component.translatable("commands." + IndustryBaseApi.MODID + ".wire.failed.not_connectable"));
	private static final SimpleCommandExceptionType ERROR_CONNECT_FAILED = new SimpleCommandExceptionType(Component.translatable("commands." + IndustryBaseApi.MODID + ".wire.failed.connect_failed"));

	static ArgumentBuilder<CommandSourceStack, ?> register() {
		return Commands.literal("wire")
				.requires(stack -> stack.hasPermission(2))
				.then(Commands.argument("from", BlockPosArgument.blockPos())
						.then(Commands.argument("to", BlockPosArgument.blockPos())
								.executes(context -> {
									CommandSourceStack source = context.getSource();
									Level level = source.getLevel();
									BlockPos from = BlockPosArgument.getLoadedBlockPos(context, "from");
									BlockPos to = BlockPosArgument.getLoadedBlockPos(context, "to");
									BlockEntity blockEntity = level.getBlockEntity(from);
									if (blockEntity instanceof IWireConnectable && level.getBlockEntity(to) instanceof IWireConnectable) {
										if (ConnectHelper.addConnect(level, from, to, blockEntity::setChanged)) {
											source.sendSuccess(() -> Component.translatable("commands." + IndustryBaseApi.MODID + ".wire.success", from.getX(), from.getY(), from.getZ(), to.getX(), to.getY(), to.getZ()), true);
										} else {
											throw ERROR_CONNECT_FAILED.create();
										}
										return Command.SINGLE_SUCCESS;
									} else {
										throw ERROR_NOT_CONNECTABLE.create();
									}
								})));
	}
}
