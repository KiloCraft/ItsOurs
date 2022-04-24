package me.drex.itsours.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import me.drex.itsours.claim.AbstractClaim;
import me.drex.itsours.util.Colors;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;

public class InfoCommand extends Command {

    public static void register(LiteralArgumentBuilder<ServerCommandSource> literal) {
        RequiredArgumentBuilder<ServerCommandSource, String> claim = ownClaimArgument();
        claim.executes(ctx -> info(ctx.getSource(), getClaim(ctx)));
        LiteralArgumentBuilder<ServerCommandSource> command = LiteralArgumentBuilder.literal("info");
        command.executes(ctx -> info(ctx.getSource(), getAndValidateClaim(ctx.getSource())));
        command.then(claim);
        literal.then(command);
    }

    public static int info(ServerCommandSource source, AbstractClaim claim) throws CommandSyntaxException {
        BlockPos size = claim.getSize();
        String world = claim.getDimension().getValue().toString();
        BlockPos tpPos = new BlockPos((claim.min.getX() + claim.max.getX()) / 2, 0, (claim.min.getZ() + claim.max.getZ()) / 2);
        String tpCommand = String.format("/execute in %s run tp @s %d ~ %d", world, tpPos.getX(), tpPos.getZ());
        source.sendFeedback(Text.translatable("text.itsours.command.info").formatted(Colors.TITLE_COLOR), false);
        source.sendFeedback(Text.translatable("text.itsours.command.info.name", claim.getName()), false);
        source.sendFeedback(Text.translatable("text.itsours.command.info.owner",
                claim.getOwner()
        ), false);
        source.sendFeedback(Text.translatable("text.itsours.command.info.size",
                Text.translatable("text.itsours.command.info.size.value", size.getX(), size.getY(), size.getZ()).formatted(Formatting.GREEN)
        ), false);
        source.sendFeedback(Text.translatable("text.itsours.command.info.depth",
                Text.literal(String.valueOf(claim.getDepth()))
        ), false);

        source.sendFeedback(Text.translatable("text.itsours.command.info.settings",
                claim.getPermissionManager().settings.toText()
        ), false);
        source.sendFeedback(Text.translatable("text.itsours.command.info.position",
                Text.translatable("text.itsours.command.info.position.min",
                        claim.min
                ).formatted(Formatting.WHITE),
                Text.translatable("text.itsours.command.info.position.max",
                        claim.max
                ).formatted(Formatting.WHITE)
        ).styled(style -> style.withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, tpCommand))), false);
        source.sendFeedback(Text.translatable("text.itsours.command.info.dimension", world), false);


        return 1;
    }

}
