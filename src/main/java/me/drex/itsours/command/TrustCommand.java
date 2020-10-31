package me.drex.itsours.command;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import me.drex.itsours.claim.AbstractClaim;
import net.minecraft.command.argument.GameProfileArgumentType;
import net.minecraft.server.command.ServerCommandSource;

public class TrustCommand extends Command {

    public static void register(LiteralArgumentBuilder<ServerCommandSource> literal) {
        {
            RequiredArgumentBuilder<ServerCommandSource, GameProfileArgumentType.GameProfileArgument> player = RequiredArgumentBuilder.argument("player", GameProfileArgumentType.gameProfile());
            player.executes(ctx -> execute(ctx.getSource(), getClaim(ctx), getGameProfile(ctx, "player"), true));
            RequiredArgumentBuilder<ServerCommandSource, String> claim = claimArgument();
            LiteralArgumentBuilder<ServerCommandSource> trust = LiteralArgumentBuilder.literal("trust");

            claim.then(player);
            trust.then(claim);
            literal.then(trust);
        }
        {
            RequiredArgumentBuilder<ServerCommandSource, GameProfileArgumentType.GameProfileArgument> player = RequiredArgumentBuilder.argument("player", GameProfileArgumentType.gameProfile());
            player.executes(ctx -> execute(ctx.getSource(), getClaim(ctx), getGameProfile(ctx, "player"), false));
            RequiredArgumentBuilder<ServerCommandSource, String> claim = claimArgument();
            LiteralArgumentBuilder<ServerCommandSource> distrust = LiteralArgumentBuilder.literal("distrust");

            claim.then(player);
            distrust.then(claim);
            literal.then(distrust);
        }
    }

    public static int execute(ServerCommandSource source, AbstractClaim claim, GameProfile target, boolean trust) throws CommandSyntaxException {
        if (trust) {
            validatePermission(claim, source.getPlayer().getUuid(), "modify.trust");
            RoleCommand.addRole(source, claim, target, "trusted", 0);
        } else {
            validatePermission(claim, source.getPlayer().getUuid(), "modify.distrust");
            RoleCommand.removeRole(source, claim, target, "trusted");
        }
        return 1;
    }
}
