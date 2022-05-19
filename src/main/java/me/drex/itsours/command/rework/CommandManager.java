package me.drex.itsours.command.rework;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.server.command.ServerCommandSource;

public class CommandManager {

    public static final CommandManager INSTANCE = new CommandManager();

    private CommandManager() {
    }

    public void register(CommandDispatcher<ServerCommandSource> dispatcher, boolean dedicated) {
        LiteralArgumentBuilder<ServerCommandSource> claim = LiteralArgumentBuilder.literal("claim_rework");

        final AbstractCommand[] commands = new AbstractCommand[]{
                BlocksCommand.INSTANCE,
                CheckCommand.INSTANCE,
                CreateCommand.INSTANCE,
                ExpandCommand.EXPAND,
                ExpandCommand.SHRINK,
                FlyCommand.INSTANCE,
                GlobalSettingCommand.INSTANCE,
                IgnoreCommand.INSTANCE,
                InfoCommand.INSTANCE,
                ListCommand.INSTANCE,
                PersonalSettingCommand.INSTANCE,
                SelectCommand.INSTANCE,
                SetOwnerCommand.INSTANCE,
                ShowCommand.HIDE,
                ShowCommand.SHOW,
        };

        for (AbstractCommand command : commands) {
            command.registerCommand(claim);
        }
        dispatcher.register(claim);
    }

}
