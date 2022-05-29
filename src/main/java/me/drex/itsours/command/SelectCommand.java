package me.drex.itsours.command;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import me.drex.itsours.user.ClaimPlayer;
import me.drex.itsours.user.Settings;
import net.minecraft.server.command.ServerCommandSource;

public class SelectCommand extends ToggleCommand {

    public static final SelectCommand INSTANCE = new SelectCommand();

    private SelectCommand() {
        super("select", Settings.FLIGHT, "text.itsours.commands.select");
    }

    @Override
    protected void afterToggle(ServerCommandSource src, boolean newValue) throws CommandSyntaxException {
        ((ClaimPlayer)src.getPlayer()).setSelecting(newValue);
    }
}
