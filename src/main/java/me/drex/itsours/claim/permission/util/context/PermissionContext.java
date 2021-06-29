package me.drex.itsours.claim.permission.util.context;

import me.drex.itsours.claim.permission.Permission;

import java.util.ArrayList;
import java.util.Arrays;

public class PermissionContext {

    private final Permission permission;
    private final ArrayList<ContextEntry> entries = new ArrayList<>();
    private boolean sorted = false;
    public PermissionContext(Permission permission) {
        this.permission = permission;
    }

    public ArrayList<ContextEntry> getEntries() {
        sort();
        return entries;
    }

    public void addEntry(ContextEntry contextEntry) {
        entries.add(contextEntry);
        sorted = false;
    }

    public PermissionContext combine(PermissionContext other) {
        entries.addAll(other.entries);
        sorted = false;
        return this;
    }

    public boolean getResult() {
        return getValue().value;
    }

    public Permission.Value getValue() {
        ContextEntry context = getContext();
        if (context != null) return context.getValue();
        return Permission.Value.UNSET;
    }

    private void sort() {
        if (sorted) return;
        entries.sort(ContextEntry::compareTo);
        sorted = true;
    }

    public ContextEntry getContext() {
        if (entries.isEmpty()) {
            return null;
        } else {
            sort();
            return entries.get(entries.size() - 1);
        }
    }

    @Override
    public String toString() {
        return String.format("%s[permission=%s, sorted=%s, entries=%s]", this.getClass().getSimpleName(), this.permission.asString(), sorted, Arrays.toString(entries.toArray()));
    }
}
