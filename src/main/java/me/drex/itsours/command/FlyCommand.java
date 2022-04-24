package me.drex.itsours.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import me.drex.itsours.claim.ClaimList;
import me.drex.itsours.user.PlayerList;
import me.drex.itsours.user.Settings;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.world.World;

public class FlyCommand extends Command {

    public static void register(LiteralArgumentBuilder<ServerCommandSource> command) {
        LiteralArgumentBuilder<ServerCommandSource> fly = LiteralArgumentBuilder.literal("fly");
        fly.requires(src -> hasPermission(src, "itsours.fly"));
        fly.executes(ctx -> toggleFlight(ctx.getSource()));
        command.then(fly);
    }

    public static int toggleFlight(ServerCommandSource source) throws CommandSyntaxException {
        ServerPlayerEntity player = source.getPlayer();
        boolean newValue = !PlayerList.get(player.getUuid(), Settings.FLIGHT);
        PlayerList.set(player.getUuid(), Settings.FLIGHT, newValue);
        if (ClaimList.INSTANCE.getClaimAt(player).isPresent() && player.getWorld().getRegistryKey().equals(World.OVERWORLD)) {
            player.interactionManager.getGameMode().setAbilities(player.getAbilities());
            if (newValue) {
                player.getAbilities().allowFlying = true;
            }
            player.sendAbilitiesUpdate();
        }
        MutableText text;
        if (newValue) {
            text = Text.translatable("text.itsours.command.fly.enabled").formatted(Formatting.GREEN);
        } else {
            text = Text.translatable("text.itsours.command.fly.disabled").formatted(Formatting.RED);
        }
        source.sendFeedback(text, false);
        return 1;
    }

}
