package me.drex.itsours.command.bulk;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import me.drex.itsours.ItsOursMod;
import me.drex.itsours.claim.AbstractClaim;
import me.drex.itsours.command.Command;
import me.drex.itsours.command.RemoveCommand;
import me.drex.itsours.user.ClaimPlayer;
import me.drex.itsours.util.Color;
import net.kyori.adventure.text.Component;
import net.minecraft.command.argument.BlockPosArgumentType;
import net.minecraft.command.argument.DefaultPosArgument;
import net.minecraft.command.argument.PosArgument;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.util.math.Vec3d;

import java.nio.channels.Selector;
import java.util.ArrayList;
import java.util.List;

public class UpdateCommand extends Command {

    public static void register(LiteralArgumentBuilder<ServerCommandSource> command) {
        LiteralArgumentBuilder<ServerCommandSource> update = LiteralArgumentBuilder.literal("update");
        {
            LiteralArgumentBuilder<ServerCommandSource> min = LiteralArgumentBuilder.literal("min");
            RequiredArgumentBuilder<ServerCommandSource, PosArgument> minPos = RequiredArgumentBuilder.argument("minPos", BlockPosArgumentType.blockPos());
            minPos.executes(ctx -> updateMin(ctx.getSource(), ctx.getArgument("minPos", PosArgument.class)));
            min.then(minPos);
            update.then(min);
        }
        {
            LiteralArgumentBuilder<ServerCommandSource> max = LiteralArgumentBuilder.literal("max");
            RequiredArgumentBuilder<ServerCommandSource, PosArgument> maxPos = RequiredArgumentBuilder.argument("maxPos", BlockPosArgumentType.blockPos());
            maxPos.executes(ctx -> updateMax(ctx.getSource(), ctx.getArgument("maxPos", PosArgument.class)));
            max.then(maxPos);
            update.then(max);
        }
        {
            LiteralArgumentBuilder<ServerCommandSource> remove = LiteralArgumentBuilder.literal("remove");
            remove.executes(ctx -> remove(ctx.getSource()));
            update.then(remove);

        }
        command.then(update);
    }

    public static int updateMin(ServerCommandSource source, PosArgument posArgument) throws CommandSyntaxException {
        if (posArgument instanceof DefaultPosArgument) {
            DefaultPosArgument defaultPosArgument = (DefaultPosArgument) posArgument;
            List<AbstractClaim> selectedClaims = SelectCommand.selectedClaims.getOrDefault(source.getPlayer().getUuid(), new ArrayList<>());
            for (AbstractClaim claim : selectedClaims) {
                claim.min = defaultPosArgument.toAbsoluteBlockPos(source.withPosition(new Vec3d(claim.min.getX(), claim.min.getY(), claim.min.getZ())));
            }
            ((ClaimPlayer) source.getPlayer()).sendMessage(Component.text("Updated " + selectedClaims.size() + " claim" + (selectedClaims.size() == 1 ? "" : "s") + "!").color(Color.LIGHT_GREEN));
        }
        return 1;
    }

    public static int updateMax(ServerCommandSource source, PosArgument posArgument) throws CommandSyntaxException {
        if (posArgument instanceof DefaultPosArgument) {
            DefaultPosArgument defaultPosArgument = (DefaultPosArgument) posArgument;
            List<AbstractClaim> selectedClaims = SelectCommand.selectedClaims.getOrDefault(source.getPlayer().getUuid(), new ArrayList<>());
            for (AbstractClaim claim : selectedClaims) {
                claim.max = defaultPosArgument.toAbsoluteBlockPos(source.withPosition(new Vec3d(claim.max.getX(), claim.max.getY(), claim.max.getZ())));
            }
            ((ClaimPlayer) source.getPlayer()).sendMessage(Component.text("Updated " + selectedClaims.size() + " claim" + (selectedClaims.size() == 1 ? "" : "s") + "!").color(Color.LIGHT_GREEN));
        }
        return 1;
    }

    public static int remove(ServerCommandSource source) throws CommandSyntaxException {
        List<AbstractClaim> selectedClaims = SelectCommand.selectedClaims.getOrDefault(source.getPlayer().getUuid(), new ArrayList<>());
        for (AbstractClaim claim : selectedClaims) {
            RemoveCommand.removeClaim(claim);
        }
        SelectCommand.selectedClaims.put(source.getPlayer().getUuid(), new ArrayList<>());
        SelectCommand.updatePageCache(source.getPlayer().getUuid());
        ((ClaimPlayer) source.getPlayer()).sendMessage(Component.text("Deleted " + selectedClaims.size() + " claim" + (selectedClaims.size() == 1 ? "" : "s") + "!").color(Color.RED));
        return 1;
    }
}
