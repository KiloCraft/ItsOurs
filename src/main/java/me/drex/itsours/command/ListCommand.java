package me.drex.itsours.command;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import me.drex.itsours.claim.Claim;
import me.drex.itsours.claim.list.ClaimList;
import net.minecraft.command.argument.GameProfileArgumentType;
import net.minecraft.server.command.ServerCommandSource;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static me.drex.itsours.util.PlaceholderUtil.*;
import static me.drex.message.api.LocalizedMessage.localized;
import static net.minecraft.server.command.CommandManager.argument;

public class ListCommand extends AbstractCommand {

    public static final ListCommand INSTANCE = new ListCommand();

    private ListCommand() {
        super("list");
    }

    @Override
    protected void register(LiteralArgumentBuilder<ServerCommandSource> literal) {
        literal.then(
                argument("target", GameProfileArgumentType.gameProfile())
                    .executes(ctx -> executeList(ctx.getSource(), GameProfileArgumentType.getProfileArgument(ctx, "target")))
            )
            .executes(ctx -> executeList(ctx.getSource(), Collections.singleton(ctx.getSource().getPlayerOrThrow().getGameProfile())));
    }

    private int executeList(ServerCommandSource src, Collection<GameProfile> targets) {
        int i = 0;
        for (GameProfile target : targets) {
            List<Claim> claims = ClaimList.getClaimsFrom(target.getId());
            i += claims.size();
            if (claims.isEmpty()) {
                src.sendFeedback(() -> localized("text.itsours.commands.list.empty", gameProfile("target_", target)), false);
            } else {
                src.sendFeedback(() -> localized("text.itsours.commands.list", mergePlaceholderMaps(
                    Map.of("claims", list(claims, claim -> claim.placeholders(src.getServer()), "text.itsours.commands.list")),
                    gameProfile("target_", target)
                )), false);
            }
        }
        return i;
    }
}
