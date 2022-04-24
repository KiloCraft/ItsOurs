package me.drex.itsours.command;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import me.drex.itsours.claim.AbstractClaim;
import me.drex.itsours.claim.permission.Permission;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

public class TrustCommand extends Command {

    public static void register(LiteralArgumentBuilder<ServerCommandSource> literal, CommandDispatcher<ServerCommandSource> dispatcher) {
        {
            LiteralArgumentBuilder<ServerCommandSource> trust = LiteralArgumentBuilder.literal("trust");
            RequiredArgumentBuilder<ServerCommandSource, String> player = playerArgument("player");
            player.executes(ctx -> execute(ctx, Permission.Value.TRUE));
            RequiredArgumentBuilder<ServerCommandSource, String> claim = permissionClaimArgument("modify.trust");

            claim.then(player);
            trust.then(claim);
            dispatcher.register(trust);
            literal.then(trust);
        }
        {
            RequiredArgumentBuilder<ServerCommandSource, String> player = playerArgument("player");
            player.executes(ctx -> execute(ctx, Permission.Value.FALSE));
            RequiredArgumentBuilder<ServerCommandSource, String> claim = permissionClaimArgument("modify.distrust");
            LiteralArgumentBuilder<ServerCommandSource> distrust = LiteralArgumentBuilder.literal("distrust");

            claim.then(player);
            distrust.then(claim);
            literal.then(distrust);
        }
        {
            LiteralArgumentBuilder<ServerCommandSource> untrust = LiteralArgumentBuilder.literal("untrust");
            RequiredArgumentBuilder<ServerCommandSource, String> player = playerArgument("player");
            player.executes(ctx -> execute(ctx, Permission.Value.UNSET));
            RequiredArgumentBuilder<ServerCommandSource, String> claim = permissionClaimArgument("modify.untrust");

            claim.then(player);
            untrust.then(claim);
            dispatcher.register(untrust);
            literal.then(untrust);
        }
    }

    public static int execute(ServerCommandSource source, AbstractClaim claim, GameProfile target, Permission.Value trust) throws CommandSyntaxException {
        switch (trust) {
            case TRUE -> {
                validatePermission(claim, source, "modify.trust");
                if (claim.getOwner().equals(target.getId()))
                    throw new SimpleCommandExceptionType(Text.translatable("text.itsours.commands.exception.trust.self")).create();
                RoleCommand.addRole(source, claim, target, "trusted", 0);
            }
            case FALSE -> {
                validatePermission(claim, source, "modify.distrust");
                RoleCommand.removeRole(source, claim, target, "trusted");
            }
            case UNSET -> {
                validatePermission(claim, source, "modify.untrust");
                RoleCommand.unsetRole(source, claim, target, "trusted");
            }
        }
        return 1;
    }

    public static int execute(CommandContext<ServerCommandSource> ctx, Permission.Value trust) throws CommandSyntaxException {
        return execute(ctx, getClaim(ctx), trust);
    }

    public static int execute(CommandContext<ServerCommandSource> ctx, AbstractClaim claim, Permission.Value trust) {
        ServerCommandSource src = ctx.getSource();
        getGameProfile(ctx, "player", profile -> execute(src, claim, profile, trust));
        return 1;
    }
}
