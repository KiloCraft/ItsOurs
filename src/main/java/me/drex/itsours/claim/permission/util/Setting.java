package me.drex.itsours.claim.permission.util;

public class Setting extends Permission {


    Setting (String id) {
        super(id);
        this.defaultValue = Value.UNSET;
    }

    Setting (String id, Value defaultValue) {
        super(id);
        this.defaultValue = defaultValue;
    }

    public static boolean isValid(String setting) {
        return Permission.isValid(setting) || getPermission(setting) instanceof Setting;
    }
}
