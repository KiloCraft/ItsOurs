package me.drex.itsours.claim.permission.util.context;

import me.drex.itsours.ItsOurs;
import me.drex.itsours.claim.AbstractClaim;
import me.drex.itsours.claim.permission.Permission;
import org.jetbrains.annotations.NotNull;

public class ContextEntry implements Comparable<ContextEntry> {

    private final AbstractClaim claim;
    private final Priority priority;
    private final Permission permission;
    private final Permission.Value value;
    public ContextEntry(AbstractClaim claim, Priority priority, Permission permission, Permission.Value value) {
        this.claim = claim;
        this.priority = priority;
        this.permission = permission;
        this.value = value;
    }

    public static ContextEntry of(AbstractClaim claim, Priority priority, Permission permission, Permission.Value value) {
        return new ContextEntry(claim, priority, permission, value);
    }

    public AbstractClaim getClaim() {
        return claim;
    }

    public Priority getPriority() {
        return priority;
    }

    public Permission getPermission() {
        return permission;
    }

    public Permission.Value getValue() {
        return value;
    }

    @Override
    public int compareTo(@NotNull ContextEntry o) {
        int i = Integer.compare(claim.getDepth(), o.claim.getDepth());
        if (i != 0) return i;
        int j = priority.compareTo(o.priority);
        if (j != 0) return j;
        int k = Integer.compare(permission.nodes(), o.permission.nodes());
        if (k != 0) return k;
        for (int l = 0; l < permission.nodes(); l++) {
            if (o.permission.getNodes().get(l).contains(permission.getNodes().get(l).getId())) return 1;
            if (permission.getNodes().get(l).contains(o.permission.getNodes().get(l).getId())) return -1;
        }
        ItsOurs.LOGGER.warn(this + " and " + o + " could not be differentiated!");
        return 0;
    }

    @Override
    public String toString() {
        return String.format("%s[claim=%s, priority=%s, permission=%s, value=%s]", this.getClass().getSimpleName(), this.claim.toShortString(), this.priority.toString(), this.permission.asString(), this.value.toString());
    }
}
