package me.drex.itsours.command;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import me.drex.itsours.claim.AbstractClaim;
import me.drex.itsours.claim.permission.Permission;
import net.minecraft.command.argument.GameProfileArgumentType;
import net.minecraft.server.command.ServerCommandSource;

public class TrustCommand extends Command {

    public static void register(LiteralArgumentBuilder<ServerCommandSource> literal) {
        {
            RequiredArgumentBuilder<ServerCommandSource, GameProfileArgumentType.GameProfileArgument> player = RequiredArgumentBuilder.argument("player", GameProfileArgumentType.gameProfile());
            player.executes(ctx -> execute(ctx.getSource(), getClaim(ctx), getGameProfile(ctx, "player"), Permission.Value.TRUE));
            RequiredArgumentBuilder<ServerCommandSource, String> claim = ownClaimArgument();
            LiteralArgumentBuilder<ServerCommandSource> trust = LiteralArgumentBuilder.literal("trust");

            claim.then(player);
            trust.then(claim);
            literal.then(trust);
        }
        {
            RequiredArgumentBuilder<ServerCommandSource, GameProfileArgumentType.GameProfileArgument> player = RequiredArgumentBuilder.argument("player", GameProfileArgumentType.gameProfile());
            player.executes(ctx -> execute(ctx.getSource(), getClaim(ctx), getGameProfile(ctx, "player"), Permission.Value.FALSE));
            RequiredArgumentBuilder<ServerCommandSource, String> claim = ownClaimArgument();
            LiteralArgumentBuilder<ServerCommandSource> distrust = LiteralArgumentBuilder.literal("distrust");

            claim.then(player);
            distrust.then(claim);
            literal.then(distrust);
        }
        {
            RequiredArgumentBuilder<ServerCommandSource, GameProfileArgumentType.GameProfileArgument> player = RequiredArgumentBuilder.argument("player", GameProfileArgumentType.gameProfile());
            player.executes(ctx -> execute(ctx.getSource(), getClaim(ctx), getGameProfile(ctx, "player"), Permission.Value.UNSET));
            RequiredArgumentBuilder<ServerCommandSource, String> claim = ownClaimArgument();
            LiteralArgumentBuilder<ServerCommandSource> untrust = LiteralArgumentBuilder.literal("untrust");

            claim.then(player);
            untrust.then(claim);
            literal.then(untrust);
        }
    }

    public static int execute(ServerCommandSource source, AbstractClaim claim, GameProfile target, Permission.Value trust) throws CommandSyntaxException {
        switch (trust) {
            case TRUE:
                validatePermission(claim, source.getPlayer().getUuid(), "modify.trust");
                RoleCommand.addRole(source, claim, target, "trusted", 0);
                break;
            case FALSE:
                validatePermission(claim, source.getPlayer().getUuid(), "modify.distrust");
                RoleCommand.removeRole(source, claim, target, "trusted");
                break;
            case UNSET:
                validatePermission(claim, source.getPlayer().getUuid(), "modify.untrust");
                RoleCommand.unsetRole(source, claim, target, "trusted");
                break;
        }
        return 1;
    }
}
