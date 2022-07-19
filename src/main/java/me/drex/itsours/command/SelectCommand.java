package me.drex.itsours.command;

import me.drex.itsours.user.Settings;

public class SelectCommand extends ToggleCommand {

    public static final SelectCommand INSTANCE = new SelectCommand();

    private SelectCommand() {
        super("select", Settings.SELECT, "text.itsours.commands.select");
    }

}
