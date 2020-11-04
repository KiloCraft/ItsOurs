package me.drex.itsours.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import me.drex.itsours.claim.AbstractClaim;
import me.drex.itsours.user.ClaimPlayer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;

public class ShowCommand extends Command {

    public static void register(LiteralArgumentBuilder<ServerCommandSource> literal) {
        {
            LiteralArgumentBuilder<ServerCommandSource> show = LiteralArgumentBuilder.literal("show");
            show.executes(ctx -> show(ctx.getSource(), true));
            literal.then(show);
        }
        {
            LiteralArgumentBuilder<ServerCommandSource> hide = LiteralArgumentBuilder.literal("hide");
            hide.executes(ctx -> show(ctx.getSource(), false));
            literal.then(hide);
        }
    }

    public static int show(ServerCommandSource source, boolean show) throws CommandSyntaxException {
        AbstractClaim claim = getAndValidateClaim(source.getWorld(), source.getPlayer().getBlockPos());
        ServerPlayerEntity player = source.getPlayer();
        ClaimPlayer claimPlayer = (ClaimPlayer) player;
        if (claimPlayer.getLastShowClaim() != null) claimPlayer.getLastShowClaim().show(player, false);
        claimPlayer.setLastShow(claim, source.getPlayer().getBlockPos(), source.getWorld());
        claim.show(player, show);
        return 1;
    }

}
