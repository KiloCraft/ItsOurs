package me.drex.itsours.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import me.drex.itsours.ItsOurs;
import me.drex.itsours.claim.AbstractClaim;
import me.drex.itsours.claim.Claim;
import me.drex.itsours.claim.ClaimList;
import me.drex.itsours.claim.Subzone;
import me.drex.itsours.command.argument.ClaimArgument;
import me.drex.itsours.user.PlayerList;
import me.drex.itsours.user.Settings;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import static net.minecraft.server.command.CommandManager.literal;

public class RemoveCommand extends AbstractCommand {

    public static final RemoveCommand INSTANCE = new RemoveCommand();

    public static final String LITERAL = "remove";
    public static final String LITERAL_CONFIRM = "confirm";

    private RemoveCommand() {
        super(LITERAL);
    }

    @Override
    protected void register(LiteralArgumentBuilder<ServerCommandSource> literal) {
        literal.then(
                        ClaimArgument.ownClaims()
                                .then(
                                        literal(LITERAL_CONFIRM)
                                                .executes(ctx -> executeRemoveConfirmed(ctx.getSource(), ClaimArgument.getClaim(ctx)))
                                )
                                .executes(ctx -> executeRemove(ctx.getSource(), ClaimArgument.getClaim(ctx)))
                )
                .executes(ctx -> executeRemove(ctx.getSource(), getClaim(ctx.getSource().getPlayer())));
    }

    private int executeRemove(ServerCommandSource src, AbstractClaim claim) throws CommandSyntaxException {
        MutableText text = Text.translatable("text.itsours.commands.remove.info",
                claim.getFullName(),
                Text.translatable("text.itsours.commands.remove.info.confirm")
                        .formatted(Formatting.DARK_GREEN, Formatting.BOLD)
                        .styled(style -> style
                                .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, String.format("/%s %s %s %s", CommandManager.LITERAL, LITERAL, claim.getFullName(), LITERAL_CONFIRM)))
                                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.translatable("text.itsours.commands.remove.info.confirm.hover").formatted(Formatting.RED)))
                        )
        );
        if (claim.getOwner().equals(src.getEntityOrThrow().getUuid())) {
            src.sendFeedback(text, false);
            return 1;
        } else if (ItsOurs.hasPermission(src, "remove")) {
            src.sendFeedback(Text.translatable("text.itsours.commands.remove.warning").formatted(Formatting.DARK_RED, Formatting.BOLD), false);
            src.sendFeedback(text, false);
            return 2;
        } else {
            src.sendError(Text.translatable("text.itsours.commands.remove.error"));
            return -1;
        }
    }

    private int executeRemoveConfirmed(ServerCommandSource src, AbstractClaim claim) throws CommandSyntaxException {
        if (claim.getOwner().equals(src.getEntityOrThrow().getUuid()) || ItsOurs.hasPermission(src, "remove")) {
            removeClaim(claim);
            src.sendFeedback(Text.translatable("text.itsours.commands.remove.success", claim.getFullName()), false);
            return 1;
        } else {
            src.sendError(Text.translatable("text.itsours.commands.remove.error"));
            return -1;
        }
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

}
