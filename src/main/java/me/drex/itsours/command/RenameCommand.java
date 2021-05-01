package me.drex.itsours.command;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import me.drex.itsours.ItsOursMod;
import me.drex.itsours.claim.AbstractClaim;
import me.drex.itsours.claim.Claim;
import me.drex.itsours.claim.Subzone;
import me.drex.itsours.user.ClaimPlayer;
import me.drex.itsours.util.Color;
import me.drex.itsours.util.TextComponentUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.minecraft.server.command.ServerCommandSource;

public class RenameCommand extends Command {

    public static void register(LiteralArgumentBuilder<ServerCommandSource> literal) {
        RequiredArgumentBuilder<ServerCommandSource, String> newOwner = RequiredArgumentBuilder.argument("newName", StringArgumentType.word());
        newOwner.executes(ctx -> rename(ctx.getSource(), getClaim(ctx), StringArgumentType.getString(ctx, "newName")));
        RequiredArgumentBuilder<ServerCommandSource, String> claim = ownClaimArgument();
        claim.then(newOwner);
        LiteralArgumentBuilder<ServerCommandSource> command = LiteralArgumentBuilder.literal("rename");
        command.then(claim);
        literal.then(command);
    }

    public static int rename(ServerCommandSource source, AbstractClaim claim, String newName) throws CommandSyntaxException {
        validatePermission(claim, source.getPlayer().getUuid(), "modify.name");
        if (claim instanceof Subzone) {
            AbstractClaim parent = ((Subzone) claim).getParent();
            for (Subzone subzone : parent.getSubzones()) {
                if (subzone.getName().equals(newName)) throw new SimpleCommandExceptionType(TextComponentUtil.error("Claim name is already taken")).create();
            }
        } else {
            if (ItsOursMod.INSTANCE.getClaimList().contains(newName)) throw new SimpleCommandExceptionType(TextComponentUtil.error("Claim name is already taken")).create();
        }
        if (!AbstractClaim.isNameValid(newName)) throw new SimpleCommandExceptionType(TextComponentUtil.error("Claim name is to long or contains invalid characters")).create();
        TextComponent text = Component.text("Changed name of ").color(Color.YELLOW)
                .append(Component.text(claim.getName()).color(Color.ORANGE))
                .append(Component.text(" to ").color(Color.YELLOW))
                .append(Component.text(newName).color(Color.ORANGE));
        ((ClaimPlayer)source.getPlayer()).sendMessage(text);
        claim.setName(newName);
        return 1;
    }

}

