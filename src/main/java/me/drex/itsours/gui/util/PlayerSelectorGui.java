package me.drex.itsours.gui.util;

import eu.pb4.sgui.api.elements.GuiElementBuilder;
import me.drex.itsours.gui.GuiContext;
import net.minecraft.item.Items;
import net.minecraft.text.Text;
import org.apache.commons.lang3.StringUtils;

import java.util.function.Consumer;

public class PlayerSelectorGui extends StringInputGui {

    private final Consumer<String> consumer;
    private int ticksUntilDynamic = Integer.MAX_VALUE;

    public PlayerSelectorGui(GuiContext context, Consumer<String> consumer) {
        super(context);
        this.consumer = consumer;
    }

    protected void build() {
        build(false);
    }

    private void build(boolean dynamicProfile) {
        String input = getInput();
        GuiElementBuilder builder = new GuiElementBuilder(Items.PLAYER_HEAD)
            .setName(Text.literal(input))
            .setCallback(() -> {
                if (!StringUtils.isBlank(input)) {
                    consumer.accept(input);
                    backCallback();
                }
            })
            .hideDefaultTooltip();
        if (dynamicProfile) {
            builder.setProfile(input);
        } else {
            builder.setProfileSkinTexture(GuiTextures.GUI_QUESTION_MARK);
        }
        setSlot(2,
            builder
        );
    }

    @Override
    public void onTick() {
        super.onTick();
        if (ticksUntilDynamic == 0) {
            build(true);
        } else if (ticksUntilDynamic > 0) {
            ticksUntilDynamic--;
        }
    }

    @Override
    public void onInput(String input) {
        super.onInput(input);
        ticksUntilDynamic = 20;
    }
}
