package me.drex.itsours.command;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import me.drex.itsours.ItsOursMod;
import me.drex.itsours.claim.AbstractClaim;
import me.drex.itsours.claim.Claim;
import net.minecraft.block.Blocks;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.util.math.Direction;

import java.util.UUID;

import static net.minecraft.server.command.CommandManager.argument;

public class ExpandCommand extends Command {

    public void register(LiteralArgumentBuilder<ServerCommandSource> literal) {
        {
            RequiredArgumentBuilder<ServerCommandSource, Integer> amount = argument("distance", IntegerArgumentType.integer(1));
            amount.executes(ctx -> expand(ctx, true));
            LiteralArgumentBuilder<ServerCommandSource> expand = LiteralArgumentBuilder.literal("expand");
            expand.then(amount);
            literal.then(expand);
        }
        {
            RequiredArgumentBuilder<ServerCommandSource, Integer> amount = argument("distance", IntegerArgumentType.integer(1));
            amount.executes(ctx -> expand(ctx, false));
            LiteralArgumentBuilder<ServerCommandSource> shrink = LiteralArgumentBuilder.literal("shrink");
            shrink.then(amount);
            literal.then(shrink);
        }


    }

    public int expand(CommandContext<ServerCommandSource> ctx, boolean expand) throws CommandSyntaxException {
        ServerCommandSource source = ctx.getSource();
        UUID uuid = source.getPlayer().getUuid();
        AbstractClaim claim = this.getAndValidateClaim(source.getWorld(), source.getPlayer().getBlockPos());
        int amount = IntegerArgumentType.getInteger(ctx, "distance");
        amount *= expand ? 1 : -1;
        Direction direction = Direction.getEntityFacingOrder(source.getPlayer())[0];
        int blocks = claim.expand(uuid, direction, amount);
        if (claim instanceof Claim) ItsOursMod.INSTANCE.getBlockManager().addBlocks(uuid, -blocks);
        //TODO: Add feedback
        return amount;
    }
}
