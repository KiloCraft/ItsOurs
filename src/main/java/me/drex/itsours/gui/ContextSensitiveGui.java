package me.drex.itsours.gui;

import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.GuiLike;
import me.drex.itsours.util.LocalizedMessage;
import net.minecraft.world.item.Items;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.sounds.SoundSource;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Component;

import java.util.Map;

public interface ContextSensitiveGui extends GuiLike {

    default MutableComponent message(String key) {
        return LocalizedMessage.localized(key, context().player);
    }

    default MutableComponent message(String key, Map<String, Component> placeholders) {
        return LocalizedMessage.localized(key, placeholders, context().player);
    }

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
            .setName(message(!context().guiStack.isEmpty() ? "text.itsours.gui.back" : "text.itsours.gui.close"))
            .hideDefaultTooltip()
            .setCallback(this::backCallback);
    }

    default void fail() {
        context().player.playSound(BuiltInRegistries.SOUND_EVENT.wrapAsHolder(SoundEvents.VILLAGER_NO).value(), 0.5f, 1f);
    }

    default void click() {
        context().player.playSound(SoundEvents.UI_BUTTON_CLICK.value(), 0.5f, 1f);
    }

}
