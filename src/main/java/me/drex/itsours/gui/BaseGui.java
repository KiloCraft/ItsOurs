package me.drex.itsours.gui;

import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.item.Item;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.text.Text;

import java.util.Map;

import static me.drex.message.api.LocalizedMessage.localized;

public abstract class BaseGui extends SimpleGui implements ContextSensitiveGui {

    protected GuiContext context;

    public BaseGui(GuiContext context, ScreenHandlerType<?> type) {
        super(type, context.player, false);
        this.context = context;
    }

    @Override
    public boolean open() {
        this.rebuild();
        return super.open();
    }

    @Override
    public GuiContext context() {
        return context;
    }

    public void rebuild() {
        for (int i = 0; i < this.size; i++) {
            this.clearSlot(i);
        }
        build();
        this.setSlot(size - 1, backElement());
    }

    public abstract void build();

    protected GuiElementBuilder switchElement(Item item, String name, ContextSensitiveGui gui) {
        return guiElement(item, name)
            .setCallback(() -> switchUi(gui));
    }

    protected GuiElementBuilder guiElement(Item item, String name) {
        return guiElement(item, name, Map.of());
    }

    protected GuiElementBuilder guiElement(Item item, String name, Map<String, Text> placeholders) {
        GuiElementBuilder builder = new GuiElementBuilder(item)
            .setName(localized("text.itsours.gui." + name + ".name", placeholders))
            .hideDefaultTooltip();
        builder.addLoreLine(localized("text.itsours.gui." + name + ".lore", placeholders));
        return builder;
    }

}
