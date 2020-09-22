package me.drex.itsours.command;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import me.drex.itsours.ItsOursMod;
import me.drex.itsours.claim.AbstractClaim;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.util.math.Direction;

import java.util.UUID;

import static net.minecraft.server.command.CommandManager.argument;

public class ExpandCommand extends Command {

    public void register(LiteralArgumentBuilder<ServerCommandSource> literal) {
        RequiredArgumentBuilder<ServerCommandSource, Integer> amount = argument("distance", IntegerArgumentType.integer(1, 1024));
        amount.executes(this::expand);
        LiteralArgumentBuilder<ServerCommandSource> command = LiteralArgumentBuilder.literal("expand");
        command.then(amount);
        literal.then(command);

    }

    public int expand(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        ServerCommandSource source = ctx.getSource();
        UUID uuid = source.getPlayer().getUuid();
        AbstractClaim claim = ItsOursMod.INSTANCE.getClaimList().get(source.getWorld(), source.getPlayer().getBlockPos());
        int amount = IntegerArgumentType.getInteger(ctx, "distance");
        Direction direction = Direction.getEntityFacingOrder(source.getPlayer())[0];
        int blocks = claim.expand(uuid, direction, amount);
        ItsOursMod.INSTANCE.getBlockManager().addBlocks(uuid, -blocks);
        //TODO: Add feedback
        return amount;
    }
}
