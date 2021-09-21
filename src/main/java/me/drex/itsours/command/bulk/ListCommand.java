package me.drex.itsours.command.bulk;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import me.drex.itsours.command.Command;
import me.drex.itsours.user.ClaimPlayer;
import me.drex.itsours.util.Color;
import me.drex.itsours.util.TextPage;
import net.kyori.adventure.text.Component;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;

public class ListCommand extends Command {

    public static void register(LiteralArgumentBuilder<ServerCommandSource> command) {
        LiteralArgumentBuilder<ServerCommandSource> list = LiteralArgumentBuilder.literal("list");
        list.executes(ctx -> sendPage(ctx.getSource(), 0));
        RequiredArgumentBuilder<ServerCommandSource, Integer> page = RequiredArgumentBuilder.argument("page", IntegerArgumentType.integer(0));
        page.executes(ctx -> sendPage(ctx.getSource(), IntegerArgumentType.getInteger(ctx, "page") - 1));
        list.then(page);
        command.then(list);
    }

    public static int sendPage(ServerCommandSource source, int page) throws CommandSyntaxException {
        TextPage textPage = SelectCommand.cachedPages.get(source.getPlayer().getUuid());
        ServerPlayerEntity player = source.getPlayer();
        if (textPage != null) {
            textPage.sendPage(player, page, 5);
        } else {
            ClaimPlayer claimPlayer = (ClaimPlayer) player;
            claimPlayer.sendMessage(Component.text("You need to select claims first!").color(Color.RED));
        }
        return page;
    }

}
