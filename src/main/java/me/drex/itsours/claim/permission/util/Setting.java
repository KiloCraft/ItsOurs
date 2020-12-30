package me.drex.itsours.claim.permission.util;

public class Setting extends Permission {


    Setting(String id, String information) {
        super(id, information);
        this.defaultValue = Value.UNSET;
    }

    Setting(String id, String information, Value defaultValue) {
        super(id, information);
        this.defaultValue = defaultValue;
    }

    public static boolean isValid(String setting) {
        return Permission.isValid(setting) || getPermission(setting) instanceof Setting;
    }
}
