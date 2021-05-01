package me.drex.itsours.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import me.drex.itsours.user.ClaimPlayer;
import me.drex.itsours.util.Color;
import net.kyori.adventure.text.Component;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;

public class SelectCommand extends Command {

    public static void register(LiteralArgumentBuilder<ServerCommandSource> command) {
        LiteralArgumentBuilder<ServerCommandSource> select = LiteralArgumentBuilder.literal("select");
        select.executes(ctx -> toggleSelect(ctx.getSource()));
        command.then(select);
    }

    public static int toggleSelect(ServerCommandSource source) throws CommandSyntaxException {
        ServerPlayerEntity player = source.getPlayer();
        ClaimPlayer claimPlayer = (ClaimPlayer) player;
        boolean newVal = !claimPlayer.getSelecting();
        claimPlayer.setSelecting(newVal);
        claimPlayer.sendMessage(Component.text("Claim selecting " + (newVal ? "enabled" : "disabled")).color(newVal ? Color.LIGHT_GREEN : Color.RED));
        return 1;
    }

}
