package me.drex.itsours.util;

import net.kyori.adventure.text.Component;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class TextUtil {

    public static Component format(boolean value) {
        return Component.text(String.valueOf(value)).color(value ? Color.LIGHT_GREEN : Color.RED);
    }

}
