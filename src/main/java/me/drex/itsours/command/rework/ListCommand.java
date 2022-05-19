package me.drex.itsours.command.rework;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import me.drex.itsours.claim.AbstractClaim;
import me.drex.itsours.claim.Claim;
import me.drex.itsours.claim.ClaimList;
import net.minecraft.command.argument.GameProfileArgumentType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.text.Texts;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

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
                .executes(ctx -> executeList(ctx.getSource(), Collections.singleton(ctx.getSource().getPlayer().getGameProfile())));
    }

    private int executeList(ServerCommandSource src, Collection<GameProfile> targets) {
        int i = 0;
        for (GameProfile target : targets) {
            List<AbstractClaim> claims = ClaimList.INSTANCE.getClaimsFrom(target.getId()).stream().filter(claim -> claim instanceof Claim).toList();
            i += claims.size();
            if (claims.isEmpty()) {
                src.sendError(Text.translatable("text.itsours.commands.list.noClaims", Texts.toText(target)));
            } else {
                // TODO: Add hover
                src.sendFeedback(Text.translatable("text.itsours.commands.list",
                        Texts.toText(target),
                        Texts.join(claims, claim -> Text.literal(claim.getName()))), false);
            }
        }
        return i;
    }
}
