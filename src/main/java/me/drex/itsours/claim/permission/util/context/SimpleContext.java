package me.drex.itsours.claim.permission.util.context;

import me.drex.itsours.claim.permission.Permission;

public class SimpleContext {

    Permission.Value value = Permission.Value.UNSET;
    Reason reason = Reason.UNKNOWN;

    public SimpleContext(Permission.Value value, Reason reason) {
        this.value = value;
        this.reason = reason;
    }

    public SimpleContext() {

    }

    public Reason getReason() {
        return reason;
    }


    public Permission.Value getValue() {
        return value;
    }


    public enum Reason {
        UNKNOWN(),
        DEFAULT(),
        SETTING(),
        PERMISSION(),
        OWNER(),
        IGNORE(),
        ROLE();

        Reason() {
        }
    }

}
