package me.drex.itsours.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import me.drex.itsours.user.ClaimPlayer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class SelectCommand extends Command {

    public static void register(LiteralArgumentBuilder<ServerCommandSource> command) {
        LiteralArgumentBuilder<ServerCommandSource> select = LiteralArgumentBuilder.literal("select");
        select.executes(ctx -> toggleSelect(ctx.getSource()));
        command.then(select);
    }

    public static int toggleSelect(ServerCommandSource source) throws CommandSyntaxException {
        ServerPlayerEntity player = source.getPlayer();
        ClaimPlayer claimPlayer = (ClaimPlayer) player;
        boolean newValue = !claimPlayer.isSelecting();
        claimPlayer.setSelecting(newValue);
        MutableText text;
        if (newValue) {
            text = Text.translatable("text.itsours.command.select.enabled").formatted(Formatting.GREEN);
        } else {
            text = Text.translatable("text.itsours.command.select.disabled").formatted(Formatting.RED);
        }
        source.sendFeedback(text, false);
        return 1;
    }

}
