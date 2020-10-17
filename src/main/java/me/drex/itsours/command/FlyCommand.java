package me.drex.itsours.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import me.drex.itsours.ItsOursMod;
import me.drex.itsours.user.ClaimPlayer;
import me.drex.itsours.util.Color;
import net.kyori.adventure.text.Component;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;

public class FlyCommand extends Command {

    public static void register(LiteralArgumentBuilder<ServerCommandSource> command) {
        LiteralArgumentBuilder<ServerCommandSource> fly = LiteralArgumentBuilder.literal("fly");
        fly.requires(src -> hasPermission(src, "itsours.fly"));
        fly.executes(ctx -> toggleFlight(ctx.getSource()));
        command.then(fly);
    }

    public static int toggleFlight(ServerCommandSource source) throws CommandSyntaxException {
        ServerPlayerEntity player = source.getPlayer();
        ClaimPlayer claimPlayer = (ClaimPlayer) player;
        claimPlayer.toggleFlight();
        if (ItsOursMod.INSTANCE.getClaimList().get(player.getServerWorld(), player.getBlockPos()) != null) {
            player.interactionManager.getGameMode().setAbilities(player.abilities);
            if (claimPlayer.flightEnabled()) {
                player.abilities.allowFlying = true;
            }
            player.sendAbilitiesUpdate();
        }
        claimPlayer.sendMessage(Component.text("Claim flight " + (claimPlayer.flightEnabled() ? "enabled" : "disabled")).color(claimPlayer.flightEnabled() ? Color.LIGHT_GREEN : Color.RED));
        return 1;
    }

}
