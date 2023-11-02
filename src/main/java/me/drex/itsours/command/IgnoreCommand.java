package me.drex.itsours.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import me.drex.itsours.ItsOurs;
import me.drex.itsours.user.PlayerData;
import net.minecraft.server.command.ServerCommandSource;

public class IgnoreCommand extends ToggleCommand {

    public static final IgnoreCommand INSTANCE = new IgnoreCommand();

    private IgnoreCommand() {
        super("ignore", PlayerData::ignore, PlayerData::setIgnore, "text.itsours.commands.ignore");
    }

    @Override
    protected void register(LiteralArgumentBuilder<ServerCommandSource> literal) {
        super.register(literal.requires(src -> ItsOurs.checkPermission(src, "itsours.ignore", 2)));
    }

}
