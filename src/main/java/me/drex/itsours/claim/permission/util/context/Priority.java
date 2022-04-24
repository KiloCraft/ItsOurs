package me.drex.itsours.claim.permission.util.context;

import org.jetbrains.annotations.NotNull;

public class Priority implements Comparable<Priority> {

    public static final Priority DEFAULT = new Priority("Default", 1);
    public static final Priority SETTING = new Priority("Setting", 2);
    public static final Priority PERMISSION = new Priority("Permission", 4);
    public static final Priority OWNER = new Priority("Owner", 5);
    public static final Priority IGNORE = new Priority("Ignore", 6);
    private final String name;
    private final int priority;
    private final int weight;

    public Priority(String name, int priority, int weight) {
        this.name = name;
        this.priority = priority;
        this.weight = weight;
    }

    public Priority(String name, int priority) {
        this(name, priority, 0);
    }

    public static Priority of(String name, int priority, int weight) {
        return new Priority(name, priority, weight);
    }

    public String getName() {
        return name;
    }

    public int getPriority() {
        return priority;
    }

    public int getWeight() {
        return weight;
    }

    @Override
    public String toString() {
        return String.format("%s[name=%s, priority=%s, weight=%s]", this.getClass().getSimpleName(), this.name, this.priority, this.weight);
    }

    @Override
    public int compareTo(@NotNull Priority o) {
        int i = Integer.compare(priority, o.getPriority());
        if (i != 0) return i;
        return Integer.compare(weight, o.getWeight());
    }
}
