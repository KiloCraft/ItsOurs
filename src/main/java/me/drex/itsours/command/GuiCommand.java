package me.drex.itsours.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import me.drex.itsours.ItsOurs;
import me.drex.itsours.claim.AbstractClaim;
import me.drex.itsours.claim.flags.Flags;
import me.drex.itsours.command.argument.ClaimArgument;
import me.drex.itsours.gui.ClaimGui;
import me.drex.itsours.gui.GuiContext;
import me.drex.itsours.gui.claims.PlayerClaimListGui;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.GameProfileArgument;
import net.minecraft.server.players.NameAndId;
import net.minecraft.commands.CommandSourceStack;

import java.util.Collection;
import java.util.List;

import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

public class GuiCommand extends AbstractCommand {

    public static final GuiCommand ADVANCED = new GuiCommand(true);
    public static final GuiCommand SIMPLE = new GuiCommand(false);
    private final boolean advanced;

    private GuiCommand(boolean advanced) {
        super(advanced ? "gui-advanced" : "gui");
        this.advanced = advanced;
    }

    @Override
    protected void register(LiteralArgumentBuilder<CommandSourceStack> literal) {
        literal.then(
                literal("-player").then(
                    argument("target", GameProfileArgument.gameProfile())
                        .executes(ctx -> executeOpenGui(ctx.getSource(), GameProfileArgument.getGameProfiles(ctx, "target")))
                        .requires(src -> ItsOurs.checkPermission(src, "itsours.gui.others", 2))
                )
            ).then(
                ClaimArgument.ownClaims()
                    .executes(ctx -> {
                        AbstractClaim claim = ClaimArgument.getClaim(ctx);
                        validateAction(ctx.getSource(), claim, Flags.MODIFY);
                        new ClaimGui(new GuiContext(ctx.getSource().getPlayerOrException()), claim, advanced).open();
                        return 1;
                    })
            )
            .executes(ctx -> executeOpenGui(ctx.getSource(), List.of(ctx.getSource().getPlayerOrException().nameAndId())));
    }

    private int executeOpenGui(CommandSourceStack src, Collection<NameAndId> targets) throws CommandSyntaxException {
        if (targets.isEmpty()) throw EntityArgument.NO_PLAYERS_FOUND.create();
        if (targets.size() > 1) throw EntityArgument.ERROR_NOT_SINGLE_PLAYER.create();
        PlayerClaimListGui gui = new PlayerClaimListGui(new GuiContext(src.getPlayerOrException()), targets.iterator().next().id(), advanced);
        gui.open();
        return 1;
    }

}
