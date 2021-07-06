package me.drex.itsours.command;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import me.drex.itsours.claim.AbstractClaim;
import me.drex.itsours.claim.permission.Permission;
import net.minecraft.command.argument.GameProfileArgumentType;
import net.minecraft.server.command.ServerCommandSource;

public class TrustCommand extends Command {

    public static void register(LiteralArgumentBuilder<ServerCommandSource> literal) {
        {
            RequiredArgumentBuilder<ServerCommandSource, GameProfileArgumentType.GameProfileArgument> player = RequiredArgumentBuilder.argument("player", GameProfileArgumentType.gameProfile());
            player.executes(ctx -> execute(ctx, Permission.Value.TRUE));
            RequiredArgumentBuilder<ServerCommandSource, String> claim = permissionClaimArgument("modify.trust");
            LiteralArgumentBuilder<ServerCommandSource> trust = LiteralArgumentBuilder.literal("trust");

            claim.then(player);
            trust.then(claim);
            literal.then(trust);
        }
        {
            RequiredArgumentBuilder<ServerCommandSource, GameProfileArgumentType.GameProfileArgument> player = RequiredArgumentBuilder.argument("player", GameProfileArgumentType.gameProfile());
            player.executes(ctx -> execute(ctx, Permission.Value.FALSE));
            RequiredArgumentBuilder<ServerCommandSource, String> claim = permissionClaimArgument("modify.distrust");
            LiteralArgumentBuilder<ServerCommandSource> distrust = LiteralArgumentBuilder.literal("distrust");

            claim.then(player);
            distrust.then(claim);
            literal.then(distrust);
        }
        {
            RequiredArgumentBuilder<ServerCommandSource, GameProfileArgumentType.GameProfileArgument> player = RequiredArgumentBuilder.argument("player", GameProfileArgumentType.gameProfile());
            player.executes(ctx -> execute(ctx, Permission.Value.UNSET));
            RequiredArgumentBuilder<ServerCommandSource, String> claim = permissionClaimArgument("modify.untrust");
            LiteralArgumentBuilder<ServerCommandSource> untrust = LiteralArgumentBuilder.literal("untrust");

            claim.then(player);
            untrust.then(claim);
            literal.then(untrust);
        }
    }

    public static int execute(ServerCommandSource source, AbstractClaim claim, GameProfile target, Permission.Value trust) throws CommandSyntaxException {
        switch (trust) {
            case TRUE -> {
                validatePermission(claim, source.getPlayer().getUuid(), "modify.trust");
                RoleCommand.addRole(source, claim, target, "trusted", 0);
            }
            case FALSE -> {
                validatePermission(claim, source.getPlayer().getUuid(), "modify.distrust");
                RoleCommand.removeRole(source, claim, target, "trusted");
            }
            case UNSET -> {
                validatePermission(claim, source.getPlayer().getUuid(), "modify.untrust");
                RoleCommand.unsetRole(source, claim, target, "trusted");
            }
        }
        return 1;
    }

    public static int execute(CommandContext<ServerCommandSource> ctx, Permission.Value trust) throws CommandSyntaxException {
        ServerCommandSource src = ctx.getSource();
        AbstractClaim claim = getClaim(ctx);
        getGameProfile(ctx, "player", profile -> execute(src, claim, profile, trust));
        return 1;
    }
}
