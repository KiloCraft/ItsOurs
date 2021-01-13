package me.drex.itsours.claim.permission.util;

public class Setting extends Permission {


    Setting (String id) {
        super(id);
    }

<<<<<<< Updated upstream
    Setting (String id, Value defaultValue) {
        super(id);
=======
    Setting(String id) {
        super(id);
        this.defaultValue = Value.UNSET;
    }

    Setting(String id, String information, Value defaultValue) {
        super(id, information);
>>>>>>> Stashed changes
        this.defaultValue = defaultValue;
    }

    public static boolean isVaid(String setting) {
        return Permission.isValid(setting) || getPermission(setting) instanceof Setting;
    }
}
