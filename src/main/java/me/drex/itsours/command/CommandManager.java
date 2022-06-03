package me.drex.itsours.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.server.command.ServerCommandSource;

public class CommandManager {

    public static final CommandManager INSTANCE = new CommandManager();

    public static final String LITERAL = "claim";

    private CommandManager() {
    }

    public void register(CommandDispatcher<ServerCommandSource> dispatcher, boolean dedicated) {
        LiteralArgumentBuilder<ServerCommandSource> claim = LiteralArgumentBuilder.literal(LITERAL);

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
                MessageCommand.INSTANCE,
                PersonalSettingCommand.INSTANCE,
                RemoveCommand.INSTANCE,
                RenameCommand.INSTANCE,
                RolesCommand.INSTANCE,
                SelectCommand.INSTANCE,
                SetOwnerCommand.INSTANCE,
                ShowCommand.HIDE,
                ShowCommand.SHOW,
                TrustCommand.TRUST,
                TrustCommand.DISTRUST,
                TrustedCommand.INSTANCE,
        };

        for (AbstractCommand command : commands) {
            command.registerCommand(claim);
        }
        dispatcher.register(claim);
    }

}
