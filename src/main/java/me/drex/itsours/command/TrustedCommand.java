package me.drex.itsours.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import me.drex.itsours.claim.AbstractClaim;
import me.drex.itsours.claim.groups.ClaimGroupManager;
import me.drex.itsours.claim.groups.Group;
import me.drex.itsours.command.argument.ClaimArgument;
import net.minecraft.commands.CommandSourceStack;

import static me.drex.itsours.util.LocalizedMessage.localized;

public class TrustedCommand extends AbstractCommand {

    public static final TrustedCommand INSTANCE = new TrustedCommand();

    public TrustedCommand() {
        super("trusted");
    }

    @Override
    protected void register(LiteralArgumentBuilder<CommandSourceStack> literal) {
        literal.then(
                ClaimArgument.ownClaims()
                    .executes(ctx -> executeTrusted(ctx.getSource(), ClaimArgument.getClaim(ctx)))
            )
            .executes(ctx -> executeTrusted(ctx.getSource(), getClaim(ctx.getSource())));
    }

    private int executeTrusted(CommandSourceStack src, AbstractClaim claim) {
        if (claim.getGroupManager().trusted.players().isEmpty()) {
            src.sendFailure(localized("text.itsours.commands.trusted.empty"));
        } else {
            src.sendSuccess(() -> localized("text.itsours.commands.trusted", claim.placeholders(src.getServer())), false);
        }
        return 1;
    }

}
