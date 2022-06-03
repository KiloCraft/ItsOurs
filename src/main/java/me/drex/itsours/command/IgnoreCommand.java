package me.drex.itsours.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import me.drex.itsours.user.Settings;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.minecraft.server.command.ServerCommandSource;

public class IgnoreCommand extends ToggleCommand {

    public static final IgnoreCommand INSTANCE = new IgnoreCommand();

    @Override
    protected void register(LiteralArgumentBuilder<ServerCommandSource> literal) {
        super.register(literal.requires(src -> Permissions.check(src, "itsours.ignore")));
    }

    private IgnoreCommand() {
        super("ignore", Settings.FLIGHT, "text.itsours.commands.ignore");
    }

}
