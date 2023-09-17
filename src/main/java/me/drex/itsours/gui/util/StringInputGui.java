package me.drex.itsours.gui.util;

import eu.pb4.sgui.api.gui.AnvilInputGui;
import me.drex.itsours.gui.ContextSensitiveGui;
import me.drex.itsours.gui.GuiContext;

public abstract class StringInputGui extends AnvilInputGui implements ContextSensitiveGui {

    protected final GuiContext context;

    public StringInputGui(GuiContext context) {
        super(context.player, false);
        this.context = context;
    }

    @Override
    public boolean open() {
        build();
        setSlot(1, backElement());
        return super.open();
    }

    @Override
    public void onInput(String input) {
        super.onInput(input);
        build();
    }

    protected abstract void build();

    @Override
    public GuiContext context() {
        return context;
    }
}
