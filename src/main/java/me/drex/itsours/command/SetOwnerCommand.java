package me.drex.itsours.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import me.drex.itsours.ItsOurs;
import me.drex.itsours.claim.AbstractClaim;
import me.drex.itsours.command.argument.ClaimArgument;
import me.drex.itsours.util.PlaceholderUtil;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.GameProfileArgument;
import net.minecraft.server.players.NameAndId;
import net.minecraft.commands.CommandSourceStack;

import java.util.Collection;

import static me.drex.itsours.util.LocalizedMessage.localized;
import static net.minecraft.commands.Commands.argument;

public class SetOwnerCommand extends AbstractCommand {

    public static final SetOwnerCommand INSTANCE = new SetOwnerCommand();

    private SetOwnerCommand() {
        super("setowner");
    }

    @Override
    protected void register(LiteralArgumentBuilder<CommandSourceStack> literal) {
        literal.then(
            ClaimArgument.allClaims().then(
                argument("owner", GameProfileArgument.gameProfile())
                    .executes(ctx -> execute(ctx.getSource(), ClaimArgument.getClaim(ctx), GameProfileArgument.getGameProfiles(ctx, "owner")))
            )
        ).requires(src -> ItsOurs.checkPermission(src, "itsours.setowner", 2));
    }

    private int execute(CommandSourceStack src, AbstractClaim claim, Collection<NameAndId> targets) throws CommandSyntaxException {
        if (targets.isEmpty()) throw EntityArgument.NO_PLAYERS_FOUND.create();
        if (targets.size() > 1) throw EntityArgument.ERROR_NOT_SINGLE_PLAYER.create();
        NameAndId profile = targets.iterator().next();
        claim.getMainClaim().setOwner(profile.id());
        src.sendSuccess(() -> localized("text.itsours.commands.setowner", PlaceholderUtil.mergePlaceholderMaps(
            claim.placeholders(src.getServer()),
            PlaceholderUtil.gameProfile("target_", profile)
        )), false);
        return 1;
    }

}
