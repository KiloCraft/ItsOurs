package me.drex.itsours.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import me.drex.itsours.ItsOurs;
import me.drex.itsours.user.Settings;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.minecraft.server.command.ServerCommandSource;

public class IgnoreCommand extends ToggleCommand {

    public static final IgnoreCommand INSTANCE = new IgnoreCommand();

    @Override
    protected void register(LiteralArgumentBuilder<ServerCommandSource> literal) {
        super.register(literal.requires(src -> ItsOurs.hasPermission(src, "ignore")));
    }

    private IgnoreCommand() {
        super("ignore", Settings.IGNORE, "text.itsours.commands.ignore");
    }

}
