package me.drex.itsours.gui.util;

import eu.pb4.sgui.api.elements.GuiElementBuilder;
import me.drex.itsours.gui.GuiContext;
import net.minecraft.item.Items;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.apache.commons.lang3.StringUtils;

import java.util.function.Consumer;
import java.util.function.Predicate;

public class ValidStringInputGui extends StringInputGui {


    private final Predicate<String> isValid;
    private final Consumer<String> inputConsumer;

    public ValidStringInputGui(GuiContext context, String defaultInput, Predicate<String> isValid, Consumer<String> inputConsumer) {
        super(context);
        this.isValid = isValid;
        this.inputConsumer = inputConsumer;
        this.setDefaultInputValue(defaultInput);
    }

    @Override
    public void build() {
        String input = getInput();
        boolean validNewName = isValid.test(input);
        setSlot(2,
            new GuiElementBuilder(validNewName ? Items.GREEN_STAINED_GLASS_PANE : Items.RED_STAINED_GLASS_PANE)
                .setName(Text.literal(input).formatted(validNewName ? Formatting.GREEN : Formatting.RED))
                .setCallback(() -> {
                    if (validNewName) {
                        if (!StringUtils.isBlank(input)) {
                            inputConsumer.accept(input);
                        }
                    } else {
                        fail();
                    }
                })
                .hideFlags()
        );
    }


}
