package me.drex.itsours.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import me.drex.itsours.claim.AbstractClaim;
import me.drex.itsours.claim.Claim;
import me.drex.itsours.claim.ClaimList;
import me.drex.itsours.claim.Subzone;
import me.drex.itsours.user.PlayerList;
import me.drex.itsours.user.Settings;
import net.minecraft.text.Text;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.ClickEvent;
import net.minecraft.util.Formatting;

public class RemoveCommand extends Command {

    public static void register(LiteralArgumentBuilder<ServerCommandSource> literal) {
        LiteralArgumentBuilder<ServerCommandSource> confirm = LiteralArgumentBuilder.literal("confirm");
        confirm.executes(ctx -> remove(ctx.getSource(), getClaim(ctx)));
        RequiredArgumentBuilder<ServerCommandSource, String> claim = ownClaimArgument();
        claim.executes(ctx -> requestRemove(ctx.getSource(), getClaim(ctx)));
        LiteralArgumentBuilder<ServerCommandSource> command = LiteralArgumentBuilder.literal("remove");
        claim.then(confirm);
        command.then(claim);
        literal.then(command);
    }

    public static int requestRemove(ServerCommandSource source, AbstractClaim claim) throws CommandSyntaxException {
       validate(source, claim);
        if (source.getEntity() == null || !source.getPlayer().getUuid().equals(claim.getOwner())) {
            source.sendFeedback(Text.translatable("text.itsours.command.remove.warning").formatted(Formatting.RED, Formatting.BOLD), false);
        }
        source.sendFeedback(Text.translatable("text.itsours.command.remove.info",
                claim.getFullName(),
                Text.translatable("text.itsours.command.remove.info.confirm")
                        .styled(style -> style.withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, String.format("/claim remove %s confirm", claim.getFullName()))))
                        .formatted(Formatting.RED, Formatting.BOLD)
                ).formatted(Formatting.RED), false);
       return 0;
    }

    public static int remove(ServerCommandSource source, AbstractClaim claim) throws CommandSyntaxException {
        validate(source, claim);
        removeClaim(claim);
        source.sendFeedback(Text.translatable("text.itsours.command.remove.success", claim.getFullName()), false);
        return 1;
    }

    public static void removeClaim(AbstractClaim claim) {
        // Remove claim from its parents' subzone list
        if (claim instanceof Subzone subzone) {
            subzone.getParent().removeSubzone((Subzone) claim);
        }
        if (claim instanceof Claim) {
            int blocks = PlayerList.get(claim.getOwner(), Settings.BLOCKS);
            PlayerList.set(claim.getOwner(), Settings.BLOCKS, Math.max(0, blocks + claim.getArea()));
        }
        claim.show(false);
        // Recursively remove all subzones
        removeSubzones(claim);
        ClaimList.INSTANCE.removeClaim(claim);
    }

    public static void removeSubzones(AbstractClaim claim) {
        for (Subzone subzone : claim.getSubzones()) {
            if (!subzone.getSubzones().isEmpty()) removeSubzones(subzone);
            ClaimList.INSTANCE.removeClaim(subzone);

        }
    }

    public static void validate(ServerCommandSource source, AbstractClaim claim) throws CommandSyntaxException {
        if (source.getEntity() != null && !source.getPlayer().getUuid().equals(claim.getOwner()) && !hasPermission(source, "itsours.remove")) {
            throw new SimpleCommandExceptionType(Text.translatable("text.itsours.commands.exception.remove.cant_delete")).create();
        }
    }

}
