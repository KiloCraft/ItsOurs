package me.drex.itsours.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import me.drex.itsours.ItsOurs;
import me.drex.itsours.user.PlayerData;
import net.minecraft.commands.CommandSourceStack;

public class IgnoreCommand extends ToggleCommand {

    public static final IgnoreCommand INSTANCE = new IgnoreCommand();

    private IgnoreCommand() {
        super("ignore", PlayerData::ignore, PlayerData::setIgnore, "text.itsours.commands.ignore");
    }

    @Override
    protected void register(LiteralArgumentBuilder<CommandSourceStack> literal) {
        super.register(literal.requires(src -> ItsOurs.checkPermission(src, "itsours.ignore", 2)));
    }

}
