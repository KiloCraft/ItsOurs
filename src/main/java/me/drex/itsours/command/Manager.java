package me.drex.itsours.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.server.command.ServerCommandSource;

public class Manager {

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        LiteralArgumentBuilder<ServerCommandSource> main = LiteralArgumentBuilder.literal("claim");
        new BlocksCommand().register(main);
        new CreateCommand().register(main);
        new ExpandCommand().register(main);
        new InfoCommand().register(main);
        new ListCommand().register(main);
        new PermissionCommand().register(main);
        new RemoveCommand().register(main);
        new RoleCommand().register(main);
        new RolesCommand().register(main);
        new ShowCommand().register(main);


        dispatcher.register(main);
    }

}
