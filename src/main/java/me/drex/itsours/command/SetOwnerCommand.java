package me.drex.itsours.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import me.drex.itsours.ItsOurs;
import me.drex.itsours.claim.AbstractClaim;
import me.drex.itsours.command.argument.ClaimArgument;
import me.drex.itsours.util.PlaceholderUtil;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.command.argument.GameProfileArgumentType;
import net.minecraft.server.PlayerConfigEntry;
import net.minecraft.server.command.ServerCommandSource;

import java.util.Collection;

import static me.drex.message.api.LocalizedMessage.localized;
import static net.minecraft.server.command.CommandManager.argument;

public class SetOwnerCommand extends AbstractCommand {

    public static final SetOwnerCommand INSTANCE = new SetOwnerCommand();

    private SetOwnerCommand() {
        super("setowner");
    }

    @Override
    protected void register(LiteralArgumentBuilder<ServerCommandSource> literal) {
        literal.then(
            ClaimArgument.allClaims().then(
                argument("owner", GameProfileArgumentType.gameProfile())
                    .executes(ctx -> execute(ctx.getSource(), ClaimArgument.getClaim(ctx), GameProfileArgumentType.getProfileArgument(ctx, "owner")))
            )
        ).requires(src -> ItsOurs.checkPermission(src, "itsours.setowner", 2));
    }

    private int execute(ServerCommandSource src, AbstractClaim claim, Collection<PlayerConfigEntry> targets) throws CommandSyntaxException {
        if (targets.isEmpty()) throw EntityArgumentType.PLAYER_NOT_FOUND_EXCEPTION.create();
        if (targets.size() > 1) throw EntityArgumentType.TOO_MANY_PLAYERS_EXCEPTION.create();
        PlayerConfigEntry profile = targets.iterator().next();
        claim.getMainClaim().setOwner(profile.id());
        src.sendFeedback(() -> localized("text.itsours.commands.setowner", PlaceholderUtil.mergePlaceholderMaps(
            claim.placeholders(src.getServer()),
            PlaceholderUtil.gameProfile("target_", profile)
        )), false);
        return 1;
    }

}
