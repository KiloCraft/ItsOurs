package me.drex.itsours.gui.util;

import com.mojang.authlib.GameProfile;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import me.drex.itsours.gui.GuiContext;
import net.minecraft.item.Items;
import net.minecraft.text.Text;
import org.apache.commons.lang3.StringUtils;

import java.util.UUID;
import java.util.function.Consumer;

public class PlayerSelectorGui extends StringInputGui {

    private final Consumer<String> consumer;

    public PlayerSelectorGui(GuiContext context, Consumer<String> consumer) {
        super(context);
        this.consumer = consumer;
    }

    protected void build() {
        String input = getInput();
        setSlot(2,
            new GuiElementBuilder(Items.PLAYER_HEAD)
                .setName(Text.literal(input))
                // Name must be non-empty, so we default to "DrexHD"
                // TODO 1.20.5
                .setSkullOwner(new GameProfile(UUID.randomUUID(), StringUtils.isBlank(input) ? "DrexHD" : input), null)
                .setCallback(() -> {
                    if (!StringUtils.isBlank(input)) {
                        consumer.accept(input);
                        backCallback();
                    }
                })
                .hideFlags()

        );
    }

}
