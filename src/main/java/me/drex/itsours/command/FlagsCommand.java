package me.drex.itsours.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import me.drex.itsours.claim.AbstractClaim;
import me.drex.itsours.claim.flags.Flags;
import me.drex.itsours.claim.flags.Flag;
import me.drex.itsours.claim.flags.context.GlobalContext;
import me.drex.itsours.claim.flags.node.Node;
import me.drex.itsours.claim.flags.util.Modify;
import me.drex.itsours.claim.flags.util.Value;
import me.drex.itsours.command.argument.ClaimArgument;
import me.drex.itsours.gui.GuiContext;
import me.drex.itsours.gui.flags.FlagsGui;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

import java.util.Map;

import static me.drex.itsours.command.argument.FlagArgument.*;
import static me.drex.itsours.util.PlaceholderUtil.mergePlaceholderMaps;
import static me.drex.message.api.LocalizedMessage.localized;
import static net.minecraft.server.command.CommandManager.literal;

public class FlagsCommand extends AbstractCommand {

    public static final FlagsCommand INSTANCE = new FlagsCommand();

    public FlagsCommand() {
        super("flags");
    }

    @Override
    protected void register(LiteralArgumentBuilder<ServerCommandSource> literal) {
        literal.then(
            ClaimArgument.ownClaims()
                .then(literal("check").then(
                        flag()
                            .executes(ctx -> executeCheck(ctx.getSource(), ClaimArgument.getClaim(ctx), getFlag(ctx)))
                    )
                ).then(
                    literal("set").then(
                        flag().then(
                            value()
                                .executes(ctx -> executeSet(ctx.getSource(), ClaimArgument.getClaim(ctx), getFlag(ctx), getValue(ctx)))
                        )
                    )
                ).executes(context -> {
                    new FlagsGui(new GuiContext(context.getSource().getPlayerOrThrow()), ClaimArgument.getClaim(context), Flag.flag()).open();
                    return 1;
                })
        );
    }

    public int executeSet(ServerCommandSource src, AbstractClaim claim, Flag flag, Value value) throws CommandSyntaxException {
        validateAction(src, claim, Flags.MODIFY, Modify.FLAG.node());
        flag.validateContext(new Node.ChangeContext(claim, GlobalContext.INSTANCE, value, src));
        claim.getFlags().set(flag, value);
        src.sendFeedback(() -> localized("text.itsours.commands.flags.set", mergePlaceholderMaps(
                Map.of(
                    "flag", Text.literal(flag.asString()),
                    "value", value.format()
                ),
                claim.placeholders(src.getServer())
            )
        ), false);
        return 1;
    }

    public int executeCheck(ServerCommandSource src, AbstractClaim claim, Flag flag) throws CommandSyntaxException {
        validateAction(src, claim, Flags.MODIFY, Modify.FLAG.node());
        Value value = claim.getFlags().get(flag);
        src.sendFeedback(() -> localized("text.itsours.commands.flags.check", mergePlaceholderMaps(
                Map.of(
                    "flag", Text.literal(flag.asString()),
                    "value", value.format()
                ),
                claim.placeholders(src.getServer())
            )
        ), false);
        return 1;
    }

}
