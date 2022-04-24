// TODO: Rewrite
/*
package me.drex.itsours.command.bulk;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import me.drex.itsours.command.Command;
import net.minecraft.server.command.ServerCommandSource;

import static net.minecraft.server.command.CommandManager.literal;

public class BulkCommand extends Command {

    public static void register(LiteralArgumentBuilder<ServerCommandSource> command) {
        LiteralArgumentBuilder<ServerCommandSource> bulk = literal("bulk");
        bulk.requires(src -> hasPermission(src, "itsours.bulk"));
        ListCommand.register(bulk);
        UpdateCommand.register(bulk);
        SelectCommand.register(bulk);
        command.then(bulk);
    }

}
*/
