package me.drex.itsours.command;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import me.drex.itsours.ItsOursMod;
import me.drex.itsours.claim.AbstractClaim;
import me.drex.itsours.claim.Claim;
import me.drex.itsours.claim.Subzone;
import me.drex.itsours.user.ClaimPlayer;
import me.drex.itsours.util.Color;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.HoverEvent;
import net.minecraft.command.argument.GameProfileArgumentType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.*;
import net.minecraft.util.Formatting;

import java.util.List;

public class ListCommand extends Command {

    public void register(LiteralArgumentBuilder<ServerCommandSource> command) {
        RequiredArgumentBuilder<ServerCommandSource, GameProfileArgumentType.GameProfileArgument> player = RequiredArgumentBuilder.argument("player", GameProfileArgumentType.gameProfile());
        player.requires(src -> this.hasPermission(src, "itsours.list"));
        player.executes(ctx -> list(ctx.getSource(), Command.getGameProfile(ctx, "player")));
        LiteralArgumentBuilder<ServerCommandSource> list = LiteralArgumentBuilder.literal("list");
        list.executes(ctx -> list(ctx.getSource(), ctx.getSource().getPlayer().getGameProfile()));
        list.then(player);
        command.then(list);
    }

    public int list(ServerCommandSource source, GameProfile target) throws CommandSyntaxException {
        List<AbstractClaim> claims = ItsOursMod.INSTANCE.getClaimList().get(target.getId());
        if (claims.isEmpty()) {
            ((ClaimPlayer) source.getPlayer()).sendMessage(Component.text("No claims").color(Color.RED));
            return 0;
        }
        TextComponent.Builder builder = Component.text().content("Claims (" + target.getName() + ") \n").color(Color.ORANGE);
        boolean color = false;
        boolean color2 = false;
        for (AbstractClaim claim : claims) {
            if (claim instanceof Claim) {
                TextComponent.Builder hover = Component.text();
                if (!claim.getSubzones().isEmpty()) {
                    hover.append(Component.text("Subzones: \n").color(Color.LIGHT_BLUE));
                    for (Subzone subzone : claim.getSubzones()) {
                        hover.append(Component.text(subzone.getName() + " ").color(color2 ? Color.PURPLE : Color.DARK_PURPLE));
                        color2 = !color2;
                    }
                }
                builder.append(Component.text(claim.getName() + " ").color(color ? Color.AQUA : Color.BLUE).clickEvent(net.kyori.adventure.text.event.ClickEvent.runCommand("/claim info " + claim.getName())).style((style) -> style.hoverEvent(HoverEvent.showText(hover.build()))));
                color = !color;
            }
        }
        ((ClaimPlayer) source.getPlayer()).sendMessage(builder.build());
        return claims.size();
    }
}
