package me.drex.itsours.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import me.drex.itsours.claim.Claim;
import me.drex.itsours.claim.list.ClaimList;
import net.minecraft.commands.arguments.GameProfileArgument;
import net.minecraft.server.players.NameAndId;
import net.minecraft.commands.CommandSourceStack;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static me.drex.itsours.util.PlaceholderUtil.*;
import static me.drex.itsours.util.LocalizedMessage.localized;
import static net.minecraft.commands.Commands.argument;

public class ListCommand extends AbstractCommand {

    public static final ListCommand INSTANCE = new ListCommand();

    private ListCommand() {
        super("list");
    }

    @Override
    protected void register(LiteralArgumentBuilder<CommandSourceStack> literal) {
        literal.then(
                argument("target", GameProfileArgument.gameProfile())
                    .executes(ctx -> executeList(ctx.getSource(), GameProfileArgument.getGameProfiles(ctx, "target")))
            )
            .executes(ctx -> executeList(ctx.getSource(), Collections.singleton(ctx.getSource().getPlayerOrException().nameAndId())));
    }

    private int executeList(CommandSourceStack src, Collection<NameAndId> targets) {
        int i = 0;
        for (NameAndId target : targets) {
            List<Claim> claims = ClaimList.getClaimsFrom(target.id());
            i += claims.size();
            if (claims.isEmpty()) {
                src.sendSuccess(() -> localized("text.itsours.commands.list.empty", gameProfile("target_", target)), false);
            } else {
                src.sendSuccess(() -> localized("text.itsours.commands.list", mergePlaceholderMaps(
                    Map.of("claims", list(claims, claim -> claim.placeholders(src.getServer()), "text.itsours.commands.list")),
                    gameProfile("target_", target)
                )), false);
            }
        }
        return i;
    }
}
