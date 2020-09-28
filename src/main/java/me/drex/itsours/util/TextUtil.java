package me.drex.itsours.util;

import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class TextUtil {

    public static Text format(boolean value) {
        return new LiteralText(String.valueOf(value)).formatted(value ? Formatting.GREEN : Formatting.RED);
    }

}
