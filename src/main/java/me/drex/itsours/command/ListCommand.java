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
            ((ClaimPlayer) source.getPlayer()).sendMessage(new LiteralText("No claims").formatted(Formatting.RED));
            return 0;
        }
        MutableText text = new LiteralText("Claims (" + target.getName() + ") \n").formatted(Formatting.GOLD);
        boolean color = false;
        boolean color2 = false;
        for (AbstractClaim claim : claims) {
            if (claim instanceof Claim) {
                MutableText hover = new LiteralText("");
                if (!claim.getSubzones().isEmpty()) {
                    hover.append("Subzones: \n").formatted(Formatting.BLUE);
                    for (Subzone subzone : claim.getSubzones()) {
                        hover.append(new LiteralText(subzone.getName() + " ").formatted(color2 ? Formatting.LIGHT_PURPLE : Formatting.DARK_PURPLE));
                        color2 = !color2;
                    }
                }
                text.append(new LiteralText(claim.getName() + " ").formatted(color ? Formatting.AQUA : Formatting.DARK_AQUA).styled(style -> style.withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/claim info " + claim.getName())).withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, hover))));
                color = !color;
            }
        }
        ((ClaimPlayer) source.getPlayer()).sendMessage(text);
        return claims.size();
    }
}
