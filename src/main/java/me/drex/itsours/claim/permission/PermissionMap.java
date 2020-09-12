package me.drex.itsours.claim.permission;

import java.util.HashMap;

public class PermissionMap extends HashMap<String, Boolean> {

    public boolean hasPermission(String permission) {
        return this.containsKey(permission) && this.get(permission);
    }

}
