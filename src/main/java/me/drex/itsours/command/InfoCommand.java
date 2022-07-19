package me.drex.itsours.command;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import me.drex.itsours.claim.AbstractClaim;
import me.drex.itsours.command.argument.ClaimArgument;
import me.drex.itsours.util.Components;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

import java.util.Optional;

public class InfoCommand extends AbstractCommand {

    public static final InfoCommand INSTANCE = new InfoCommand();

    private InfoCommand() {
        super("info");
    }

    @Override
    protected void register(LiteralArgumentBuilder<ServerCommandSource> literal) {
        literal.then(
                        ClaimArgument.ownClaims()
                                .executes(ctx -> executeInfo(ctx.getSource(), ClaimArgument.getClaim(ctx)))
                )
                .executes(ctx -> executeInfo(ctx.getSource(), getClaim(ctx.getSource().getPlayer())));
    }

    // TODO: Add trusted info
    private int executeInfo(ServerCommandSource src, AbstractClaim claim) {
        Optional<GameProfile> optional = src.getServer().getUserCache().getByUuid(claim.getOwner());
        src.sendFeedback(Text.translatable("text.itsours.commands.info",
                claim.getFullName(),
                Components.toText(optional.orElse(new GameProfile(claim.getOwner(), null))),
                Components.toText(claim.getSize()),
                claim.getDepth(),
                claim.getPermissionHolder().getSettings().toText(),
                Components.toText(claim.getBox()),
                claim.getDimension().getValue().toString()
        ), false);
        return 1;
    }

}
