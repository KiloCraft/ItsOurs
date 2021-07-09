package me.drex.itsours.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import me.drex.itsours.claim.AbstractClaim;
import me.drex.itsours.gui.screen.InfoScreen;
import net.minecraft.server.command.ServerCommandSource;

public class GUICommand extends Command {

    public static void register(LiteralArgumentBuilder<ServerCommandSource> literal) {
        RequiredArgumentBuilder<ServerCommandSource, String> claim = ownClaimArgument();
        claim.executes(ctx -> gui(ctx.getSource(), getClaim(ctx)));
        LiteralArgumentBuilder<ServerCommandSource> command = LiteralArgumentBuilder.literal("gui");
        command.executes(ctx -> gui(ctx.getSource(), getAndValidateClaim(ctx.getSource())));
        command.then(claim);
        literal.then(command);
    }

    public static int gui(ServerCommandSource source, AbstractClaim claim) throws CommandSyntaxException {
        InfoScreen infoScreen = new InfoScreen(source.getPlayer(), () -> claim);
        infoScreen.render();
        return 1;
    }

}
