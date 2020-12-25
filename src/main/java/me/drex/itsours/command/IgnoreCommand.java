package me.drex.itsours.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import me.drex.itsours.ItsOursMod;
import me.drex.itsours.user.ClaimPlayer;
import me.drex.itsours.user.PlayerSetting;
import me.drex.itsours.util.Color;
import net.kyori.adventure.text.Component;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;

public class IgnoreCommand extends Command {

    public static void register(LiteralArgumentBuilder<ServerCommandSource> command) {
        LiteralArgumentBuilder<ServerCommandSource> ignore = LiteralArgumentBuilder.literal("ignore");
        ignore.requires(src -> hasPermission(src, "itsours.ignore"));
        ignore.executes(ctx -> toggleIgnore(ctx.getSource()));
        command.then(ignore);
    }

    public static int toggleIgnore(ServerCommandSource source) throws CommandSyntaxException {
        ServerPlayerEntity player = source.getPlayer();
        ClaimPlayer claimPlayer = (ClaimPlayer) player;
        boolean val = !(boolean)ItsOursMod.INSTANCE.getPlayerList().getBoolean(player.getUuid(), PlayerSetting.IGNORE);
        ItsOursMod.INSTANCE.getPlayerList().setBoolean(player.getUuid(), PlayerSetting.IGNORE, val);
        claimPlayer.sendMessage(Component.text("Claim ignore " + (val ? "enabled" : "disabled")).color(val ? Color.LIGHT_GREEN : Color.RED));
        return 1;
    }

}
