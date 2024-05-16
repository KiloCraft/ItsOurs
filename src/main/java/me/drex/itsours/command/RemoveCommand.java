package me.drex.itsours.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import me.drex.itsours.ItsOurs;
import me.drex.itsours.claim.AbstractClaim;
import me.drex.itsours.claim.Claim;
import me.drex.itsours.claim.list.ClaimList;
import me.drex.itsours.claim.Subzone;
import me.drex.itsours.command.argument.ClaimArgument;
import me.drex.itsours.data.DataManager;
import me.drex.itsours.user.ClaimTrackingPlayer;
import me.drex.itsours.user.PlayerData;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;

import static me.drex.message.api.LocalizedMessage.localized;
import static net.minecraft.server.command.CommandManager.literal;

public class RemoveCommand extends AbstractCommand {

    public static final RemoveCommand INSTANCE = new RemoveCommand();

    private RemoveCommand() {
        super("remove");
    }

    public static void removeClaim(ServerCommandSource src, AbstractClaim claim) {
        // Remove claim from its parents' subzone list
        if (claim instanceof Subzone subzone) {
            subzone.getParent().removeSubzone((Subzone) claim);
        }
        if (claim instanceof Claim) {
            PlayerData userData = DataManager.updateUserData(claim.getOwner());
            userData.setBlocks(Math.max(0, userData.blocks() + claim.getArea()));
        }
        // Recursively remove all subzones
        removeSubzones(claim);
        ClaimList.removeClaim(claim);
        claim.notifyTrackingChanges(src.getServer(), false);
    }

    public static void removeSubzones(AbstractClaim claim) {
        for (Subzone subzone : claim.getSubzones()) {
            if (!subzone.getSubzones().isEmpty()) removeSubzones(subzone);
            ClaimList.removeClaim(subzone);
        }
    }

    @Override
    protected void register(LiteralArgumentBuilder<ServerCommandSource> literal) {
        literal.then(
                ClaimArgument.ownClaims()
                    .then(
                        literal("confirm")
                            .executes(ctx -> executeRemoveConfirmed(ctx.getSource(), ClaimArgument.getClaim(ctx)))
                    )
                    .executes(ctx -> executeRemove(ctx.getSource(), ClaimArgument.getClaim(ctx)))
            )
            .executes(ctx -> executeRemove(ctx.getSource(), getClaim(ctx.getSource().getPlayer())));
    }

    public int executeRemove(ServerCommandSource src, AbstractClaim claim) throws CommandSyntaxException {
        if (claim.getOwner().equals(src.getEntityOrThrow().getUuid()) || ItsOurs.checkPermission(src, "itsours.remove", 2)) {
            src.sendFeedback(() -> localized("text.itsours.commands.remove", claim.placeholders(src.getServer())), false);
            return 1;
        } else {
            throw MISSING_PERMISSION;
        }
    }

    public int executeRemoveConfirmed(ServerCommandSource src, AbstractClaim claim) throws CommandSyntaxException {
        if (claim.getOwner().equals(src.getEntityOrThrow().getUuid()) || ItsOurs.checkPermission(src, "itsours.remove", 2)) {
            removeClaim(src, claim);
            src.sendFeedback(() -> localized("text.itsours.commands.remove.success", claim.placeholders(src.getServer())), false);
            return 1;
        } else {
            throw MISSING_PERMISSION;
        }
    }

}
