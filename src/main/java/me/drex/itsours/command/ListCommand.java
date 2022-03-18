package me.drex.itsours.command;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import me.drex.itsours.gui.screen.ListScreen;
import net.minecraft.command.argument.GameProfileArgumentType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.LiteralText;

public class ListCommand extends Command {

    public static void register(LiteralArgumentBuilder<ServerCommandSource> command) {
        RequiredArgumentBuilder<ServerCommandSource, GameProfileArgumentType.GameProfileArgument> player = players();
        player.requires(src -> hasPermission(src, "itsours.list"));
        player.executes(ListCommand::list);
        LiteralArgumentBuilder<ServerCommandSource> list = LiteralArgumentBuilder.literal("list");
        list.executes(ctx -> list(ctx.getSource(), ctx.getSource().getPlayer().getGameProfile()));
        list.then(player);
        command.then(list);
    }

    public static int list(ServerCommandSource source, GameProfile target) throws CommandSyntaxException {
        ListScreen listScreen = new ListScreen(source.getPlayer(), target.getId());
        listScreen.render();
        return 1;
    }

    public static int list(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        getGameProfile(ctx).thenAccept(optional -> {
            optional.ifPresent(gameProfile -> {
                try {
                    list(ctx.getSource(), gameProfile);
                } catch (CommandSyntaxException e) {
                    ctx.getSource().sendError(new LiteralText(e.getMessage()));
                }
            });
        });
        return 1;
    }

}
