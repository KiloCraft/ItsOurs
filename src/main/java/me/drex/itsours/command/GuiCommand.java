package me.drex.itsours.command;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import me.drex.itsours.ItsOurs;
import me.drex.itsours.gui.ClaimListGui;
import me.lucko.fabric.api.permissions.v0.Permissions;
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
                                .requires(src -> ItsOurs.hasPermission(src, "gui.others"))
                )
                .executes(ctx -> executeOpenGui(ctx.getSource(), List.of(ctx.getSource().getPlayer().getGameProfile())));
    }

    private int executeOpenGui(ServerCommandSource src, Collection<GameProfile> targets) throws CommandSyntaxException {
        if (targets.isEmpty()) throw EntityArgumentType.PLAYER_NOT_FOUND_EXCEPTION.create();
        if (targets.size() > 1) throw EntityArgumentType.TOO_MANY_PLAYERS_EXCEPTION.create();
        new ClaimListGui(src.getPlayer(), null, targets.iterator().next().getId()).open();
        return 1;
    }

}
