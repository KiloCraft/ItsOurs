package me.drex.itsours.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import me.drex.itsours.claim.AbstractClaim;
import me.drex.itsours.claim.flags.Flags;
import me.drex.itsours.claim.flags.Flag;
import me.drex.itsours.claim.flags.context.PlayerContext;
import me.drex.itsours.claim.flags.holder.FlagData;
import me.drex.itsours.claim.flags.node.Node;
import me.drex.itsours.claim.flags.util.Modify;
import me.drex.itsours.claim.flags.util.Value;
import me.drex.itsours.command.argument.ClaimArgument;
import me.drex.itsours.command.argument.FlagArgument;
import me.drex.itsours.gui.GuiContext;
import me.drex.itsours.gui.players.AdvancedPlayerManagerGui;
import net.minecraft.commands.arguments.GameProfileArgument;
import net.minecraft.server.players.NameAndId;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;

import java.util.Collection;
import java.util.Map;

import static me.drex.itsours.util.PlaceholderUtil.gameProfile;
import static me.drex.itsours.util.PlaceholderUtil.mergePlaceholderMaps;
import static me.drex.itsours.util.LocalizedMessage.localized;
import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

public class PlayerFlagsCommand extends AbstractCommand {

    public static final PlayerFlagsCommand INSTANCE = new PlayerFlagsCommand();

    public PlayerFlagsCommand() {
        super("playerflags");
    }

    @Override
    protected void register(LiteralArgumentBuilder<CommandSourceStack> literal) {
        literal.then(ClaimArgument.ownClaims().then(
                literal("check").then(
                    argument("targets", GameProfileArgument.gameProfile()).then(
                        FlagArgument.playerFlag()
                            .executes(ctx -> executeCheck(ctx.getSource(), ClaimArgument.getClaim(ctx), GameProfileArgument.getGameProfiles(ctx, "targets"), FlagArgument.getPlayerFlag(ctx)))
                    )
                )
            ).then(
                literal("set").then(
                    argument("targets", GameProfileArgument.gameProfile()).then(
                        FlagArgument.playerFlag().then(
                            FlagArgument.value()
                                .executes(ctx -> executeSet(ctx.getSource(), ClaimArgument.getClaim(ctx), GameProfileArgument.getGameProfiles(ctx, "targets"), FlagArgument.getPlayerFlag(ctx), FlagArgument.getValue(ctx)))
                        )
                    )
                )
            ).executes(context -> {
                new AdvancedPlayerManagerGui(new GuiContext(context.getSource().getPlayerOrException()), ClaimArgument.getClaim(context)).open();
                return 1;
            })
        );
    }

    public int executeSet(CommandSourceStack src, AbstractClaim claim, Collection<NameAndId> targets, Flag flag, Value value) throws CommandSyntaxException {
        validateAction(src, claim, Flags.MODIFY, Modify.FLAG.node());
        flag.validateContext(new Node.ChangeContext(claim, PlayerContext.INSTANCE, value, src));
        for (NameAndId target : targets) {
            claim.getPlayerFlags().computeIfAbsent(target.id(), (ignored) -> new FlagData()).set(flag, value);
            src.sendSuccess(() -> localized("text.itsours.commands.playerflags.set", mergePlaceholderMaps(
                    Map.of(
                        "flag", Component.literal(flag.asString()),
                        "value", value.format()
                    ),
                    gameProfile("target_", target),
                    claim.placeholders(src.getServer())
                )
            ), false);
        }
        return 1;
    }

    private int executeCheck(CommandSourceStack src, AbstractClaim claim, Collection<NameAndId> targets, Flag flag) throws CommandSyntaxException {
        validateAction(src, claim, Flags.MODIFY, Modify.FLAG.node());
        for (NameAndId target : targets) {
            Value value = claim.getPlayerFlags().getOrDefault(target.id(), new FlagData()).get(flag);
            src.sendSuccess(() -> localized("text.itsours.commands.playerflags.check", mergePlaceholderMaps(
                    Map.of(
                        "flag", Component.literal(flag.asString()),
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
