package me.drex.itsours.util;

import net.kyori.adventure.text.format.TextColor;

public class Color implements TextColor {

    public static final Color DARK_GRAY = new Color("dark_gray", 0x595959);
    public static final Color GRAY = new Color("gray", 0x7F7F7F);
    public static final Color LIGHT_GRAY = new Color("light_gray", 0xA5A5A5);
    public static final Color WHITE = new Color("white", 0xF2F2F2);
    public static final Color RED = new Color("red", 0xF94144);
    public static final Color ORANGE = new Color("orange", 0xF3722C);
    public static final Color YELLOW = new Color("yellow", 0xFFDA1F);
    public static final Color DARK_GREEN = new Color("dark_green", 0x386E0D);
    public static final Color GREEN = new Color("green",0x34EA7A);
    public static final Color LIGHT_GREEN = new Color("light_green", 0x6CF628);
    public static final Color LIGHT_BLUE = new Color("light_blue", 0x37E6E3);
    public static final Color AQUA = new Color("aqua", 0x48BFE3);
    public static final Color BLUE = new Color("blue", 0x277DA1);
    public static final Color DARK_BLUE = new Color("dark_blue", 0x30329C);
    public static final Color PINK = new Color("pink", 0xCA68C2);
    public static final Color PURPLE = new Color("purple", 0xA63A9D);
    public static final Color DARK_PURPLE = new Color("dark_purple", 0x7400B8);
    public static final Color[] COLORS = {DARK_GRAY, GRAY, LIGHT_GRAY, WHITE, RED, ORANGE, YELLOW, DARK_GREEN, GREEN, LIGHT_GREEN, LIGHT_BLUE, AQUA, BLUE, DARK_BLUE, PINK, PURPLE, DARK_PURPLE};

    public final int value;
    public final String name;
    Color(String name, int color) {
        this.name = name;
        this.value = color;
    }

    @Override
    public int value() {
        return this.value;
    }

    public String stringValue() {
        return "#" + Integer.toHexString(this.value);
    }
}
