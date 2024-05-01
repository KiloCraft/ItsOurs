package me.drex.itsours.claim.flags.util;

import net.minecraft.text.Text;

import static me.drex.message.api.LocalizedMessage.localized;

public enum Value {
    ALLOW(true, "allow", "text.itsours.value.allow"),
    DENY(false, "deny", "text.itsours.value.deny"),
    DEFAULT(false, "default", "text.itsours.value.default");

    public final boolean value;
    public final String literal;
    public final String translationId;

    Value(boolean value, String literal, String translationId) {
        this.value = value;
        this.literal = literal;
        this.translationId = translationId;
    }

    public static Value of(boolean value) {
        return value ? ALLOW : DENY;
    }

    public Value next() {
        return switch (this) {
            case ALLOW -> DENY;
            case DENY -> DEFAULT;
            case DEFAULT -> ALLOW;
        };
    }

    public Text format() {
        return localized(this.translationId);
    }
}
