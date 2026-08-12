package me.drex.itsours.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import me.drex.itsours.claim.AbstractClaim;
import me.drex.itsours.claim.flags.Flags;
import me.drex.itsours.claim.flags.util.Modify;
import me.drex.itsours.claim.groups.ClaimGroupManager;
import me.drex.itsours.command.argument.ClaimArgument;
import me.drex.itsours.util.PlaceholderUtil;
import net.minecraft.commands.arguments.GameProfileArgument;
import net.minecraft.server.players.NameAndId;
import net.minecraft.commands.CommandSourceStack;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

import static me.drex.itsours.util.LocalizedMessage.localized;
import static net.minecraft.commands.Commands.argument;

public class TrustCommand extends AbstractCommand {

    public static final TrustCommand TRUST = new TrustCommand("trust", true);
    public static final TrustCommand DISTRUST = new TrustCommand("distrust", false);

    private final boolean trust;

    private TrustCommand(@NotNull String literal, boolean trust) {
        super(literal);
        this.trust = trust;
    }

    @Override
    protected void register(LiteralArgumentBuilder<CommandSourceStack> literal) {
        literal.then(
            ClaimArgument.ownClaims()
                .then(
                    argument("targets", GameProfileArgument.gameProfile())
                        .executes(ctx -> executeTrust(ctx.getSource(), ClaimArgument.getClaim(ctx), GameProfileArgument.getGameProfiles(ctx, "targets")))
                )
        );
    }

    public int executeTrust(CommandSourceStack src, AbstractClaim claim, Collection<NameAndId> targets) throws CommandSyntaxException {
        validateAction(src, claim, Flags.MODIFY, Modify.FLAG.node());
        ClaimGroupManager groupManager = claim.getGroupManager();
        int result = 0;
        if (trust) {
            for (NameAndId target : targets) {
                if (groupManager.trusted.players().add(target.id())) {
                    src.sendSuccess(() -> localized("text.itsours.commands.trust", PlaceholderUtil.mergePlaceholderMaps(
                        PlaceholderUtil.gameProfile("target_", target),
                        claim.placeholders(src.getServer())
                    )), false);
                    result++;
                } else {
                    src.sendFailure(localized("text.itsours.commands.trust.nothingChanged", PlaceholderUtil.mergePlaceholderMaps(
                        PlaceholderUtil.gameProfile("target_", target),
                        claim.placeholders(src.getServer())
                    )));
                }
            }
        } else {
            for (NameAndId target : targets) {
                if (groupManager.trusted.players().remove(target.id())) {
                    src.sendSuccess(() -> localized("text.itsours.commands.distrust", PlaceholderUtil.mergePlaceholderMaps(
                        PlaceholderUtil.gameProfile("target_", target),
                        claim.placeholders(src.getServer())
                    )), false);
                    result++;
                } else {
                    src.sendFailure(localized("text.itsours.commands.distrust.nothingChanged", PlaceholderUtil.mergePlaceholderMaps(
                        PlaceholderUtil.gameProfile("target_", target),
                        claim.placeholders(src.getServer())
                    )));
                }
            }
        }
        return result;
    }


}
