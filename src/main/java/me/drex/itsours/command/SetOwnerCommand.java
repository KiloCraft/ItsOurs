package me.drex.itsours.command;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import me.drex.itsours.ItsOursMod;
import me.drex.itsours.claim.AbstractClaim;
import me.drex.itsours.claim.list.ClaimList;
import me.drex.itsours.user.ClaimPlayer;
import me.drex.itsours.util.Color;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.minecraft.command.argument.GameProfileArgumentType;
import net.minecraft.server.command.ServerCommandSource;

public class SetOwnerCommand extends Command {

    public static void register(LiteralArgumentBuilder<ServerCommandSource> literal) {
        RequiredArgumentBuilder<ServerCommandSource, GameProfileArgumentType.GameProfileArgument> newOwner = RequiredArgumentBuilder.argument("player", GameProfileArgumentType.gameProfile());
        newOwner.executes(SetOwnerCommand::setOwner);
        RequiredArgumentBuilder<ServerCommandSource, String> claim = allClaimArgument();
        claim.then(newOwner);
        LiteralArgumentBuilder<ServerCommandSource> command = LiteralArgumentBuilder.literal("setowner");
        command.requires(src -> hasPermission(src, "itsours.setowner"));
        command.then(claim);
        literal.then(command);
    }

    public static int setOwner(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        AbstractClaim claim = getClaim(ctx);
        getGameProfile(ctx, "player", profile -> {
            claim.setOwner(profile.getId());
            ItsOursMod.INSTANCE.getClaimList().update();
            TextComponent text = Component.text("Set owner of ").color(Color.YELLOW)
                    .append(Component.text(claim.getName()).color(Color.ORANGE))
                    .append(Component.text(" to ").color(Color.YELLOW))
                    .append(Component.text(profile.getName()).color(Color.ORANGE));
            ((ClaimPlayer)ctx.getSource().getPlayer()).sendMessage(text);
        });
        return 1;
    }

}

