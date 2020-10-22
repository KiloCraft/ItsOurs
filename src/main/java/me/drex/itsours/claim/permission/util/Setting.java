package me.drex.itsours.claim.permission.util;

public class Setting extends Permission {


    Setting(String id) {
        super(id);
    }

    public static boolean isValid(String setting) {
        return Permission.isValid(setting) || getPermission(setting) instanceof Setting;
    }
}
