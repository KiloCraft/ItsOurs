package me.drex.itsours.gui.util;

import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import me.drex.itsours.gui.ContextSensitiveGui;
import me.drex.itsours.gui.GuiContext;
import net.minecraft.item.Items;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.text.Text;

import java.util.Map;

import static me.drex.message.api.LocalizedMessage.localized;

public class ConfirmationGui extends SimpleGui implements ContextSensitiveGui {

    private final GuiContext context;
    private final String id;
    private final Map<String, Text> placeholders;
    private final Runnable runnable;

    public ConfirmationGui(GuiContext context, String id, Runnable runnable) {
        this(context, id, Map.of(), runnable);
    }

    public ConfirmationGui(GuiContext context, String id, Map<String, Text> placeholders, Runnable runnable) {
        super(ScreenHandlerType.GENERIC_9X3, context.player, false);
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
                    .setName(localized("text.itsours.gui.cancel"))
                    .hideFlags()
                    .setCallback(this::backCallback)
                );
            } else if (lineIndex > 5) {
                this.setSlot(i, new GuiElementBuilder(Items.GREEN_STAINED_GLASS_PANE)
                    .setName(localized("text.itsours.gui.confirm"))
                    .hideFlags()
                    .setCallback(() -> {
                        runnable.run();
                        backCallback();
                    })
                );
            } else {
                this.setSlot(i, new GuiElementBuilder(Items.GRAY_STAINED_GLASS_PANE)
                    .setName(localized(id, placeholders))
                    .hideFlags()
                );
            }
        }

    }

    @Override
    public GuiContext context() {
        return context;
    }
}
