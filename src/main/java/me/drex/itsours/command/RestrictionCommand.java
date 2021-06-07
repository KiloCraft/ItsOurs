package me.drex.itsours.command;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import me.drex.itsours.claim.AbstractClaim;
import net.minecraft.command.argument.GameProfileArgumentType;
import net.minecraft.server.command.ServerCommandSource;

import java.util.List;
import java.util.UUID;

public class RestrictionCommand extends Command {

    public static void register(LiteralArgumentBuilder<ServerCommandSource> literal) {
        RequiredArgumentBuilder<ServerCommandSource, String> claim = ownClaimArgument();
        LiteralArgumentBuilder<ServerCommandSource> restrict = LiteralArgumentBuilder.literal("restrict");
        LiteralArgumentBuilder<ServerCommandSource> restriction = LiteralArgumentBuilder.literal("restriction");
        {
            RequiredArgumentBuilder<ServerCommandSource, GameProfileArgumentType.GameProfileArgument> player = RequiredArgumentBuilder.argument("player", GameProfileArgumentType.gameProfile());
            LiteralArgumentBuilder<ServerCommandSource> ban = LiteralArgumentBuilder.literal("ban");
            player.executes(ctx -> ban(ctx.getSource(), getClaim(ctx), getGameProfile(ctx, "player"), true));
            ban.then(player);
            claim.then(ban);
        }
        {
            RequiredArgumentBuilder<ServerCommandSource, GameProfileArgumentType.GameProfileArgument> player = RequiredArgumentBuilder.argument("player", GameProfileArgumentType.gameProfile());
            LiteralArgumentBuilder<ServerCommandSource> unban = LiteralArgumentBuilder.literal("unban");
            player.executes(ctx -> ban(ctx.getSource(), getClaim(ctx), getGameProfile(ctx, "player"), false));
            unban.then(player);
            claim.then(unban);

        }
        claim.then(restrict);
        restriction.then(claim);
        literal.then(restriction);
    }

    public static int ban(ServerCommandSource src, AbstractClaim claim, GameProfile target, boolean ban) {
        List<UUID> banned = claim.getRestrictionManager().getBanned();
        if (ban) {
            banned.add(target.getId());
        } else {
            banned.remove(target.getId());
        }
        return 1;
    }

}
