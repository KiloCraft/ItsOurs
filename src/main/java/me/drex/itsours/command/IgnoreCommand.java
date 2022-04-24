package me.drex.itsours.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import me.drex.itsours.user.PlayerList;
import me.drex.itsours.user.Settings;
import net.minecraft.text.Text;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.MutableText;
import net.minecraft.util.Formatting;

public class IgnoreCommand extends Command {

    public static void register(LiteralArgumentBuilder<ServerCommandSource> command) {
        LiteralArgumentBuilder<ServerCommandSource> ignore = LiteralArgumentBuilder.literal("ignore");
        ignore.requires(src -> hasPermission(src, "itsours.ignore"));
        ignore.executes(ctx -> toggleIgnore(ctx.getSource()));
        command.then(ignore);
    }

    public static int toggleIgnore(ServerCommandSource source) throws CommandSyntaxException {
        ServerPlayerEntity player = source.getPlayer();
        boolean newValue = !PlayerList.get(player.getUuid(), Settings.IGNORE);
        PlayerList.set(player.getUuid(), Settings.IGNORE, newValue);
        MutableText text;
        if (newValue) {
            text = Text.translatable("text.itsours.command.ignore.enabled").formatted(Formatting.GREEN);
        } else {
            text = Text.translatable("text.itsours.command.ignore.disabled").formatted(Formatting.RED);
        }
        source.sendFeedback(text, false);
        return 1;
    }

}
