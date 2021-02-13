package me.drex.itsours.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import me.drex.itsours.command.bulk.BulkCommand;
import net.minecraft.server.command.ServerCommandSource;

public class Manager {

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        LiteralArgumentBuilder<ServerCommandSource> main = LiteralArgumentBuilder.literal("claim");
        BlocksCommand.register(main);
        BulkCommand.register(main);
        ColorCommand.register(main);
        CreateCommand.register(main);
        DebugCommand.register(main);
        ExpandCommand.register(main);
        FlyCommand.register(main);
        IgnoreCommand.register(main);
        InfoCommand.register(main);
        ListCommand.register(main);
        PermissionCommand.register(main);
        RemoveCommand.register(main);
        RenameCommand.register(main);
        RoleCommand.register(main);
        RolesCommand.register(main);
        SelectCommand.register(main);
        SetOwnerCommand.register(main);
        SettingCommand.register(main);
        ShowCommand.register(main);
        TrustCommand.register(main);
        TrustedCommand.register(main);


        dispatcher.register(main);
    }

}
