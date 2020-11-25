package me.drex.itsours.command;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import me.drex.itsours.claim.AbstractClaim;
import me.drex.itsours.user.ClaimPlayer;
import me.drex.itsours.util.Color;
import me.drex.itsours.util.TextComponentUtil;
import me.drex.itsours.util.WorldUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.TextColor;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.util.math.BlockPos;

import java.util.UUID;

public class InfoCommand extends Command {

    public static void register(LiteralArgumentBuilder<ServerCommandSource> literal) {
        RequiredArgumentBuilder<ServerCommandSource, String> claim = ownClaimArgument();
        claim.executes(ctx -> info(ctx.getSource(), getClaim(ctx)));
        LiteralArgumentBuilder<ServerCommandSource> command = LiteralArgumentBuilder.literal("info");
        command.executes(ctx -> info(ctx.getSource(), getAndValidateClaim(ctx.getSource().getWorld(), ctx.getSource().getPlayer().getBlockPos())));
        command.then(claim);
        literal.then(command);
    }

    public static int info(ServerCommandSource source, AbstractClaim claim) throws CommandSyntaxException {
        UUID ownerUUID = claim.getOwner();
        String ownerName = "";
        GameProfile owner = source.getMinecraftServer().getUserCache().getByUuid(ownerUUID);
        if (owner != null && owner.isComplete()) {
            ownerName = owner.getName();
        }
        BlockPos size = claim.getSize();

        TextComponent text = Component.text("\n")
            .append(Component.text("Claim Info: \n").color(Color.ORANGE))
            .append(newInfoLine("Name", Component.text(claim.getName()).color(Color.WHITE)))
            .append(newInfoLine("Owner", ownerName.equals("") ?
                Component.text(ownerUUID.toString()).color(Color.RED).clickEvent(net.kyori.adventure.text.event.ClickEvent.copyToClipboard(claim.getOwner().toString())) :
                TextComponentUtil.of("<gradient:" + Color.RED.stringValue() + ":" + Color.ORANGE.stringValue() + ">" + ownerName, false)
                        .clickEvent(net.kyori.adventure.text.event.ClickEvent.copyToClipboard(claim.getOwner().toString()))
                        .append(Component.text("\n"))
            .append(newInfoLine("Size", Component.text(size.getX() + " x " + size.getY() + " x " + size.getZ()).color(Color.LIGHT_GREEN)))
            .append(newInfoLine("Depth", Component.text(String.valueOf(claim.getDepth())).color(Color.DARK_GREEN)))
            .append(newInfoLine("Flags", claim.getPermissionManager().settings.toText()))
            .append(newInfoLine("Position",
                Component.text("Min ").color(Color.WHITE).append(newPosLine(claim.min, Color.AQUA, Color.BLUE)),
                Component.text(" Max ").color(Color.WHITE).append(newPosLine(claim.max, Color.PURPLE, Color.DARK_PURPLE))))
            .append(newInfoLine("Dimension", Component.text(WorldUtil.toIdentifier(claim.getWorld())).color(Color.GREEN)))));
        ((ClaimPlayer) source.getPlayer()).sendMessage(text);

        return 1;
    }

    private static Component newPosLine(BlockPos pos, TextColor color1, TextColor color2) {
        return Component.text("")
                .append(Component.text(String.valueOf(pos.getX())).color(color1))
                .append(Component.text(" "))
                .append(Component.text(String.valueOf(pos.getY())).color(color2))
                .append(Component.text(" "))
                .append(Component.text(String.valueOf(pos.getZ())).color(color1));
    }

    public static Component newInfoLine(String title, Component... text) {
        TextComponent.Builder builder = Component.text().content("* " + title + ": ").color(Color.YELLOW);
        for (Component t : text) {
            builder.append(t);
        }
        return builder.append(Component.text("\n")).build();
    }
}