package me.drex.itsours.command;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import me.drex.itsours.ItsOurs;
import me.drex.itsours.gui.GuiContext;
import me.drex.itsours.gui.claims.PlayerClaimListGui;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.command.argument.GameProfileArgumentType;
import net.minecraft.server.command.ServerCommandSource;

import java.util.Collection;
import java.util.List;

import static net.minecraft.server.command.CommandManager.argument;

public class GuiCommand extends AbstractCommand {

    public static final GuiCommand INSTANCE = new GuiCommand();

    private GuiCommand() {
        super("gui");
    }

    @Override
    protected void register(LiteralArgumentBuilder<ServerCommandSource> literal) {
        literal.then(
                argument("target", GameProfileArgumentType.gameProfile())
                    .executes(ctx -> executeOpenGui(ctx.getSource(), GameProfileArgumentType.getProfileArgument(ctx, "target")))
                    .requires(src -> ItsOurs.checkPermission(src, "itsours.gui.others", 2))
            )
            .executes(ctx -> executeOpenGui(ctx.getSource(), List.of(ctx.getSource().getPlayerOrThrow().getGameProfile())));
    }

    private int executeOpenGui(ServerCommandSource src, Collection<GameProfile> targets) throws CommandSyntaxException {
        if (targets.isEmpty()) throw EntityArgumentType.PLAYER_NOT_FOUND_EXCEPTION.create();
        if (targets.size() > 1) throw EntityArgumentType.TOO_MANY_PLAYERS_EXCEPTION.create();
        PlayerClaimListGui gui = new PlayerClaimListGui(new GuiContext(src.getPlayerOrThrow()), targets.iterator().next().getId());
        gui.open();
        return 1;
    }

}
