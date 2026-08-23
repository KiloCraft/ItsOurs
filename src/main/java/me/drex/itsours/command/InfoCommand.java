package me.drex.itsours.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import me.drex.itsours.claim.AbstractClaim;
import me.drex.itsours.claim.list.ClaimList;
import me.drex.itsours.command.argument.ClaimArgument;
import net.minecraft.commands.CommandSourceStack;

import static me.drex.itsours.util.LocalizedMessage.localized;

public class InfoCommand extends AbstractCommand {

    public static final InfoCommand INSTANCE = new InfoCommand();

    private InfoCommand() {
        super("info");
    }

    @Override
    protected void register(LiteralArgumentBuilder<CommandSourceStack> literal) {
        literal.then(
                ClaimArgument.ownClaims()
                    .executes(ctx -> executeInfo(ctx.getSource(), ClaimArgument.getClaim(ctx)))
            )
            .executes(ctx -> executeInfo(ctx.getSource(), getClaim(ctx.getSource())));
    }

    private int executeInfo(CommandSourceStack src, AbstractClaim claim) {
        src.sendSuccess(() -> localized("text.itsours.commands.info", claim.placeholders(src.getServer())), false);
        return 1;
    }

}
