package me.drex.itsours.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.server.command.ServerCommandSource;

public class CommandManager {

    public static final CommandManager INSTANCE = new CommandManager();

    public static final String LITERAL = "claim";

    private CommandManager() {
    }

    public void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess registryAccess, net.minecraft.server.command.CommandManager.RegistrationEnvironment environment) {
        LiteralArgumentBuilder<ServerCommandSource> claim = LiteralArgumentBuilder.literal(LITERAL);

        final AbstractCommand[] commands = new AbstractCommand[]{
                BlocksCommand.INSTANCE,
                CheckCommand.INSTANCE,
                CreateCommand.INSTANCE,
                ExpandCommand.EXPAND,
                ExpandCommand.SHRINK,
                FlyCommand.INSTANCE,
                GuiCommand.INSTANCE,
                SettingCommand.INSTANCE,
                IgnoreCommand.INSTANCE,
                InfoCommand.INSTANCE,
                ListCommand.INSTANCE,
                MessageCommand.INSTANCE,
                PermissionsCommand.INSTANCE,
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
