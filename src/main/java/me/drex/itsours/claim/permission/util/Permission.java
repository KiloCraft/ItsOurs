package me.drex.itsours.claim.permission.util;

public class Permission {

    public enum Value {
        TRUE(true),
        FALSE(false),
        UNSET(false);

        public final boolean value;
        Value(boolean value) {
            this.value = value;
        }

        public static Value of(boolean value) {
            return value ? TRUE : FALSE;
        }
    }

}
