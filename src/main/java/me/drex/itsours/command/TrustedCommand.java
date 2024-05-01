package me.drex.itsours.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import me.drex.itsours.claim.AbstractClaim;
import me.drex.itsours.claim.groups.ClaimGroupManager;
import me.drex.itsours.claim.groups.Group;
import me.drex.itsours.command.argument.ClaimArgument;
import net.minecraft.server.command.ServerCommandSource;

import static me.drex.message.api.LocalizedMessage.localized;

public class TrustedCommand extends AbstractCommand {

    public static final TrustedCommand INSTANCE = new TrustedCommand();

    public TrustedCommand() {
        super("trusted");
    }

    @Override
    protected void register(LiteralArgumentBuilder<ServerCommandSource> literal) {
        literal.then(
                ClaimArgument.ownClaims()
                    .executes(ctx -> executeTrusted(ctx.getSource(), ClaimArgument.getClaim(ctx)))
            )
            .executes(ctx -> executeTrusted(ctx.getSource(), getClaim(ctx.getSource().getPlayer())));
    }

    private int executeTrusted(ServerCommandSource src, AbstractClaim claim) {
        Group trusted = claim.getGroupManager().getGroup(ClaimGroupManager.TRUSTED);

        if (trusted.players().isEmpty()) {
            src.sendError(localized("text.itsours.commands.trusted.empty"));
        } else {
            src.sendFeedback(() -> localized("text.itsours.commands.trusted", claim.placeholders(src.getServer())), false);
        }
        return 1;
    }

}
