package me.drex.itsours.command.rework;

import me.drex.itsours.user.Settings;

public class IgnoreCommand extends ToggleCommand {

    public static final IgnoreCommand INSTANCE = new IgnoreCommand();

    private IgnoreCommand() {
        super("ignore", Settings.FLIGHT, "text.itsours.commands.ignore");
    }

}
