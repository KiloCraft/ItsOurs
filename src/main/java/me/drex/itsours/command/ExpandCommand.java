package me.drex.itsours.command;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import me.drex.itsours.claim.AbstractClaim;
import me.drex.itsours.claim.Claim;
import me.drex.itsours.user.PlayerList;
import me.drex.itsours.user.Settings;
import net.minecraft.text.Text;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.MutableText;
import net.minecraft.util.math.Direction;

import java.util.UUID;

import static net.minecraft.server.command.CommandManager.argument;

public class ExpandCommand extends Command {

    public static void register(LiteralArgumentBuilder<ServerCommandSource> literal) {
        {
            RequiredArgumentBuilder<ServerCommandSource, Integer> amount = argument("distance", IntegerArgumentType.integer(1));
            amount.executes(ctx -> expand(ctx, true));
            LiteralArgumentBuilder<ServerCommandSource> expand = LiteralArgumentBuilder.literal("expand");
            expand.executes(ExpandCommand::syntax);
            expand.then(amount);
            literal.then(expand);
        }
        {
            RequiredArgumentBuilder<ServerCommandSource, Integer> amount = argument("distance", IntegerArgumentType.integer(1));
            amount.executes(ctx -> expand(ctx, false));
            LiteralArgumentBuilder<ServerCommandSource> shrink = LiteralArgumentBuilder.literal("shrink");
            shrink.executes(ExpandCommand::syntax);
            shrink.then(amount);
            literal.then(shrink);
        }
    }

    public static int expand(CommandContext<ServerCommandSource> ctx, boolean expand) throws CommandSyntaxException {
        ServerCommandSource source = ctx.getSource();
        UUID uuid = source.getPlayer().getUuid();
        AbstractClaim claim = getAndValidateClaim(source);
        validatePermission(claim, source, "modify.size");
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
        ctx.getSource().sendFeedback(Text.translatable(""), false);
        MutableText text;
        if (expand) {
            text = Text.translatable("text.itsours.command.expand.expanded", claim.getName(), distance, direction.getName(), amount);
        } else {
            text = Text.translatable("text.itsours.command.expand.shrunk", claim.getName(), -distance, direction.getName(), -amount);
        }
        source.sendFeedback(text, false);
        return amount;
    }

    public static int syntax(CommandContext<ServerCommandSource> ctx) {
        ctx.getSource().sendError(Text.translatable("text.itsours.command.expand.syntax"));
        return 0;
    }

}
