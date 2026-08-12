package me.drex.itsours.gui.util;

import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import me.drex.itsours.gui.ContextSensitiveGui;
import me.drex.itsours.gui.GuiContext;
import net.minecraft.world.item.Items;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.network.chat.Component;

import java.util.Map;


public class ConfirmationGui extends SimpleGui implements ContextSensitiveGui {

    private final GuiContext context;
    private final String id;
    private final Map<String, Component> placeholders;
    private final Runnable runnable;

    public ConfirmationGui(GuiContext context, String id, Runnable runnable) {
        this(context, id, Map.of(), runnable);
    }

    public ConfirmationGui(GuiContext context, String id, Map<String, Component> placeholders, Runnable runnable) {
        super(MenuType.GENERIC_9x3, context.player, false);
        this.context = context;
        this.id = id;
        this.placeholders = placeholders;
        this.runnable = runnable;
    }

    @Override
    public boolean open() {
        build();
        return super.open();
    }

    public void build() {
        for (int i = 0; i < getSize(); i++) {
            int lineIndex = i % 9;
            if (lineIndex <= 2) {
                this.setSlot(i, new GuiElementBuilder(Items.RED_STAINED_GLASS_PANE)
                    .setName(message("text.itsours.gui.cancel"))
                    .hideDefaultTooltip()
                    .setCallback(this::backCallback)
                );
            } else if (lineIndex > 5) {
                this.setSlot(i, new GuiElementBuilder(Items.GREEN_STAINED_GLASS_PANE)
                    .setName(message("text.itsours.gui.confirm"))
                    .hideDefaultTooltip()
                    .setCallback(() -> {
                        runnable.run();
                        backCallback();
                    })
                );
            } else {
                this.setSlot(i, new GuiElementBuilder(Items.GRAY_STAINED_GLASS_PANE)
                    .setName(message(id, placeholders))
                    .hideDefaultTooltip()
                );
            }
        }

    }

    @Override
    public GuiContext context() {
        return context;
    }
}
