package me.drex.itsours.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;

public class CommandManager {

    public static final CommandManager INSTANCE = new CommandManager();

    public static final String LITERAL = "claim";

    private CommandManager() {
    }

    public void register(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext registryAccess, net.minecraft.commands.Commands.CommandSelection environment) {
        LiteralArgumentBuilder<CommandSourceStack> claim = LiteralArgumentBuilder.literal(LITERAL);

        final AbstractCommand[] commands = new AbstractCommand[]{
            BlocksCommand.INSTANCE,
            CheckCommand.INSTANCE,
            CreateCommand.INSTANCE,
            ExpandCommand.EXPAND,
            ExpandCommand.SHRINK,
            FlyCommand.INSTANCE,
            GuiCommand.SIMPLE,
            GuiCommand.ADVANCED,
            FlagsCommand.INSTANCE,
            IgnoreCommand.INSTANCE,
            InfoCommand.INSTANCE,
            ListCommand.INSTANCE,
            MessageCommand.INSTANCE,
            PlayerFlagsCommand.INSTANCE,
            RemoveCommand.INSTANCE,
            RenameCommand.INSTANCE,
            GroupsCommand.INSTANCE,
            SetOwnerCommand.INSTANCE,
            ShowCommand.HIDE,
            ShowCommand.SHOW,
            ToggleCommand.SELECT,
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
