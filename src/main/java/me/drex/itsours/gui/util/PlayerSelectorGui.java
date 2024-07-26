package me.drex.itsours.gui.util;

import com.mojang.authlib.properties.PropertyMap;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import me.drex.itsours.gui.GuiContext;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ProfileComponent;
import net.minecraft.item.Items;
import net.minecraft.text.Text;
import org.apache.commons.lang3.StringUtils;

import java.util.Optional;
import java.util.function.Consumer;

public class PlayerSelectorGui extends StringInputGui {

    private final Consumer<String> consumer;

    public PlayerSelectorGui(GuiContext context, Consumer<String> consumer) {
        super(context);
        this.consumer = consumer;
    }

    protected void build() {
        String input = getInput();
        ProfileComponent profileComponent = new ProfileComponent(StringUtils.isBlank(input) ? Optional.empty() : Optional.of(input), Optional.empty(), new PropertyMap());
        setSlot(2,
            new GuiElementBuilder(Items.PLAYER_HEAD)
                .setName(Text.literal(input))
                .setComponent(DataComponentTypes.PROFILE, profileComponent)
                .setCallback(() -> {
                    if (!StringUtils.isBlank(input)) {
                        consumer.accept(input);
                        backCallback();
                    }
                })
                .hideDefaultTooltip()

        );
    }

}
