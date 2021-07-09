package me.drex.itsours.command;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import me.drex.itsours.claim.AbstractClaim;
import me.drex.itsours.claim.Claim;
import me.drex.itsours.user.ClaimPlayer;
import me.drex.itsours.user.PlayerList;
import me.drex.itsours.user.Settings;
import me.drex.itsours.util.Color;
import net.kyori.adventure.text.Component;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.util.math.Direction;

import java.util.UUID;

import static net.minecraft.server.command.CommandManager.argument;

public class ExpandCommand extends Command {

    public static void register(LiteralArgumentBuilder<ServerCommandSource> literal) {
        {
            RequiredArgumentBuilder<ServerCommandSource, Integer> amount = argument("distance", IntegerArgumentType.integer(1));
            amount.executes(ctx -> expand(ctx, true));
            LiteralArgumentBuilder<ServerCommandSource> expand = LiteralArgumentBuilder.literal("expand");
            expand.executes(ctx -> info(ctx, true));
            expand.then(amount);
            literal.then(expand);
        }
        {
            RequiredArgumentBuilder<ServerCommandSource, Integer> amount = argument("distance", IntegerArgumentType.integer(1));
            amount.executes(ctx -> expand(ctx, false));
            LiteralArgumentBuilder<ServerCommandSource> shrink = LiteralArgumentBuilder.literal("shrink");
            shrink.executes(ctx -> info(ctx, false));
            shrink.then(amount);
            literal.then(shrink);
        }
    }

    public static int expand(CommandContext<ServerCommandSource> ctx, boolean expand) throws CommandSyntaxException {
        ServerCommandSource source = ctx.getSource();
        UUID uuid = source.getPlayer().getUuid();
        AbstractClaim claim = getAndValidateClaim(source);
        validatePermission(claim, uuid, "modify.size");
        int distance = IntegerArgumentType.getInteger(ctx, "distance");
        distance *= expand ? 1 : -1;
        Direction direction = Direction.getEntityFacingOrder(source.getPlayer())[0];
        claim.show(source.getPlayer(), false);
        int amount = claim.expand(uuid, direction, distance);
        claim.show(source.getPlayer(), true);
        if (claim instanceof Claim) {
            int blocks = PlayerList.get(uuid, Settings.BLOCKS);
            PlayerList.set(uuid, Settings.BLOCKS, Math.max(0, blocks - amount));
        }
        ((ClaimPlayer) source.getPlayer()).sendMessage(Component.text("Claim " + claim.getName() + " was " + (expand ? "expanded" : "shrunk") + " by " + Math.abs(distance) + " blocks (" + direction.getName() + ") for " + amount + " claim blocks.").color(Color.LIGHT_GREEN));
        return amount;
    }

    public static int info(CommandContext<ServerCommandSource> ctx, boolean expand) throws CommandSyntaxException {
        ServerCommandSource source = ctx.getSource();
        ((ClaimPlayer) source.getPlayer()).sendMessage(Component.text("Wrong syntax! Type /claim " + (expand ? "expand" : "shrink") + " <amount>, while looking into the direction you want to expand into.").color(Color.LIGHT_GREEN));
        return 1;
    }

}
