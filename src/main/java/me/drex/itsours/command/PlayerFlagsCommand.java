package me.drex.itsours.command;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import me.drex.itsours.claim.AbstractClaim;
import me.drex.itsours.claim.flags.FlagsManager;
import me.drex.itsours.claim.flags.Flag;
import me.drex.itsours.claim.flags.context.PlayerContext;
import me.drex.itsours.claim.flags.holder.FlagData;
import me.drex.itsours.claim.flags.node.Node;
import me.drex.itsours.claim.flags.util.Modify;
import me.drex.itsours.claim.flags.util.Value;
import me.drex.itsours.command.argument.ClaimArgument;
import me.drex.itsours.command.argument.FlagArgument;
import net.minecraft.command.argument.GameProfileArgumentType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

import java.util.Collection;
import java.util.Map;

import static me.drex.itsours.util.PlaceholderUtil.gameProfile;
import static me.drex.itsours.util.PlaceholderUtil.mergePlaceholderMaps;
import static me.drex.message.api.LocalizedMessage.localized;
import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class PlayerFlagsCommand extends AbstractCommand {

    public static final PlayerFlagsCommand INSTANCE = new PlayerFlagsCommand();

    public PlayerFlagsCommand() {
        super("playerflags");
    }

    @Override
    protected void register(LiteralArgumentBuilder<ServerCommandSource> literal) {
        literal.then(ClaimArgument.ownClaims().then(
                literal("check").then(
                    argument("targets", GameProfileArgumentType.gameProfile()).then(
                        FlagArgument.playerFlag()
                            .executes(ctx -> executeCheck(ctx.getSource(), ClaimArgument.getClaim(ctx), GameProfileArgumentType.getProfileArgument(ctx, "targets"), FlagArgument.getPlayerFlag(ctx)))
                    )
                )
            ).then(
                literal("set").then(
                    argument("targets", GameProfileArgumentType.gameProfile()).then(
                        FlagArgument.playerFlag().then(
                            FlagArgument.value()
                                .executes(ctx -> executeSet(ctx.getSource(), ClaimArgument.getClaim(ctx), GameProfileArgumentType.getProfileArgument(ctx, "targets"), FlagArgument.getPlayerFlag(ctx), FlagArgument.getValue(ctx)))
                        )
                    )
                )
            )
        );
    }

    public int executeSet(ServerCommandSource src, AbstractClaim claim, Collection<GameProfile> targets, Flag flag, Value value) throws CommandSyntaxException {
        validateAction(src, claim, FlagsManager.MODIFY, Modify.FLAG.node());
        flag.validateContext(new Node.ChangeContext(claim, PlayerContext.INSTANCE, value, src));
        for (GameProfile target : targets) {
            claim.getPlayerFlags().computeIfAbsent(target.getId(), (ignored) -> new FlagData()).set(flag, value);
            src.sendFeedback(() -> localized("text.itsours.commands.personalSetting.set", mergePlaceholderMaps(
                    Map.of(
                        "flag", Text.literal(flag.asString()),
                        "value", value.format()
                    ),
                    gameProfile("target_", target),
                    claim.placeholders(src.getServer())
                )
            ), false);
        }
        return 1;
    }

    private int executeCheck(ServerCommandSource src, AbstractClaim claim, Collection<GameProfile> targets, Flag flag) throws CommandSyntaxException {
        validateAction(src, claim, FlagsManager.MODIFY, Modify.FLAG.node());
        for (GameProfile target : targets) {
            Value value = claim.getPlayerFlags().getOrDefault(target.getId(), new FlagData()).get(flag);
            src.sendFeedback(() -> localized("text.itsours.commands.personalSetting.check", mergePlaceholderMaps(
                    Map.of(
                        "flag", Text.literal(flag.asString()),
                        "value", value.format()
                    ),
                    gameProfile("target_", target),
                    claim.placeholders(src.getServer())
                )
            ), false);
        }
        return 1;
    }

}
