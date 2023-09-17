package me.drex.itsours.claim.permission.util;

import net.minecraft.text.Text;

import static me.drex.message.api.LocalizedMessage.localized;

public enum Value {
    ALLOW(true, "allow", "text.itsours.value.allow"),
    DENY(false, "deny", "text.itsours.value.deny"),
    UNSET(false, "unset", "text.itsours.value.unset");

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
            case DENY -> UNSET;
            case UNSET -> ALLOW;
        };
    }

    public Text format() {
        return localized(this.translationId);
    }
}
