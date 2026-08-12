package me.drex.itsours.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import me.drex.itsours.user.ClaimTrackingPlayer;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerPlayer;

import static me.drex.itsours.util.LocalizedMessage.localized;

public class ShowCommand extends AbstractCommand {

    public static final ShowCommand SHOW = new ShowCommand("show", true);
    public static final ShowCommand HIDE = new ShowCommand("hide", false);

    private final boolean show;

    private ShowCommand(String literal, boolean show) {
        super(literal);
        this.show = show;
    }

    @Override
    protected void register(LiteralArgumentBuilder<CommandSourceStack> literal) {
        literal.executes(ctx -> execute(ctx.getSource()));
    }

    private int execute(CommandSourceStack src) throws CommandSyntaxException {
        ServerPlayer player = src.getPlayerOrException();
        ClaimTrackingPlayer claimTrackingPlayer = (ClaimTrackingPlayer)player;
        if (show) {
            claimTrackingPlayer.trackClaims();
            src.sendSuccess(() -> localized("text.itsours.commands.show"), false);
        } else {
            claimTrackingPlayer.unTrackClaims();
            src.sendSuccess(() -> localized("text.itsours.commands.hide"), false);
        }
        return 1;
    }

}
