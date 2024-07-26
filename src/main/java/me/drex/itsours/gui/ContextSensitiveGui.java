package me.drex.itsours.gui;

import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.GuiInterface;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;

import static me.drex.message.api.LocalizedMessage.localized;

public interface ContextSensitiveGui extends GuiInterface {

    GuiContext context();

    boolean open();

    default void switchUi(ContextSensitiveGui simpleGui) {
        switchUi(simpleGui, true);
    }

    default void switchUi(ContextSensitiveGui gui, boolean addSelf) {
        if (addSelf) {
            context().guiStack.push(this);
        }
        click();
        gui.open();
    }

    default void backCallback() {
        if (!context().guiStack.isEmpty()) {
            this.switchUi(context().guiStack.pop(), false);
        } else {
            this.close();
        }
    }

    default GuiElementBuilder backElement() {
        return new GuiElementBuilder(Items.BARRIER)
            .setName(localized(!context().guiStack.isEmpty() ? "text.itsours.gui.back" : "text.itsours.gui.close"))
            .hideDefaultTooltip()
            .setCallback(this::backCallback);
    }

    default void fail() {
        context().player.playSoundToPlayer(Registries.SOUND_EVENT.getEntry(SoundEvents.ENTITY_VILLAGER_NO).value(), SoundCategory.MASTER, 0.5f, 1f);
    }

    default void click() {
        context().player.playSoundToPlayer(SoundEvents.UI_BUTTON_CLICK.value(), SoundCategory.MASTER, 0.5f, 1f);
    }

}
