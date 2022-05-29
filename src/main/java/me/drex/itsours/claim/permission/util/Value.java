package me.drex.itsours.claim.permission.util;

import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public enum Value {
    ALLOW(true, "allow", "text.itsours.value.allow", Formatting.GREEN),
    DENY(false, "deny", "text.itsours.value.deny", Formatting.RED),
    UNSET(false, "unset", "text.itsours.value.unset", Formatting.GRAY);

    public final boolean value;
    public final String literal;
    public final String translationId;
    public final Formatting formatting;

    Value(boolean value, String literal, String translationId, Formatting formatting) {
        this.value = value;
        this.literal = literal;
        this.translationId = translationId;
        this.formatting = formatting;
    }

    public static Value of(boolean value) {
        return value ? ALLOW : DENY;
    }

    public Text format() {
        return Text.translatable(this.translationId).formatted(formatting);
    }
}
