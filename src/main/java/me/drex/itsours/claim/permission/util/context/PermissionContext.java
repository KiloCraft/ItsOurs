package me.drex.itsours.claim.permission.util.context;

import me.drex.itsours.claim.permission.Permission;
import net.minecraft.util.Pair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PermissionContext {

    private final Map<Permission, List<Pair<Priority, Permission.Value>>> data = new HashMap<>();

    public PermissionContext() {
    }

    public Map<Permission, List<Pair<Priority, Permission.Value>>> getData() {
        return data;
    }

    public void add(Permission permission, Priority reason, Permission.Value value) {
        List<Pair<Priority, Permission.Value>> list = data.getOrDefault(permission, new ArrayList<>());
        list.add(new Pair<>(reason, value));
        data.put(permission, list);
    }

    public Permission.Value getValue() {
        return getResult().getRight();
    }

    public Pair<Priority, Permission.Value> getResult() {
        Permission.Value value = Permission.Value.UNSET;
        Priority reason = CustomPriority.NONE;
        int currentPriority = -1;
        int currentDepth = -1;
        int currentWeight = -1;
        for (Map.Entry<Permission, List<Pair<Priority, Permission.Value>>> entry : data.entrySet()) {
            Permission permission = entry.getKey();
            int depth = permission.nodes();
            if (depth > currentDepth) {
                currentDepth = depth;
                for (Pair<Priority, Permission.Value> pair : entry.getValue()) {
                    int priority = pair.getLeft().getPriority();
                    int weight = pair.getLeft().getWeight();
                    if ((priority > currentPriority || (priority == currentPriority && weight > currentWeight)) && pair.getRight() != Permission.Value.UNSET) {
                        currentPriority = priority;
                        currentWeight = weight;
                        value = pair.getRight();
                        reason = pair.getLeft();
                    }
                }
            }
        }
        return new Pair<>(reason, value);
    }

    public void combine(PermissionContext other) {
        for (Map.Entry<Permission, List<Pair<Priority, Permission.Value>>> entry : other.data.entrySet()) {
            for (Pair<Priority, Permission.Value> pair : entry.getValue()) {
                this.add(entry.getKey(), pair.getLeft(), pair.getRight());
            }
        }
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder("Context=[");
        int i = 0;
        for (Map.Entry<Permission, List<Pair<Priority, Permission.Value>>> entry : data.entrySet()) {
            result.append("[permission=").append(entry.getKey().asString()).append(", ").append("list=[");
            for (int j = 0; j < entry.getValue().size(); j++) {
                Pair<Priority, Permission.Value> pair = entry.getValue().get(j);
                result.append("[").append(pair.getLeft()).append(", ").append(pair.getRight()).append("]");
                if (j != entry.getValue().size() - 1) result.append(", ");
            }
            result.append("]");
            if (i != data.entrySet().size() - 1) result.append(", ");
            i++;
        }
        result.append("]");
        return result.toString();
    }
    public static abstract class Priority {
        abstract String getName();

        abstract int getPriority();

        abstract int getWeight();

        public String toString() {
            return getName();
        }
    }

    public static class CustomPriority extends Priority {

        private final String name;
        private final int priority;

        public static CustomPriority NONE = new CustomPriority("NONE", -1);
        public static CustomPriority DEFAULT = new CustomPriority("DEFAULT", 1);
        public static CustomPriority SETTING = new CustomPriority("SETTING", 2);
        public static CustomPriority PERMISSION = new CustomPriority("PERMISSION", 4);
        public static CustomPriority OWNER = new CustomPriority("OWNER", 5);
        public static CustomPriority IGNORE = new CustomPriority("IGNORE", 6);

        public CustomPriority(String name, int priority) {
            this.name = name;
            this.priority = priority;
        }

        @Override
        public String getName() {
            return this.name;
        }

        @Override
        public int getPriority() {
            return this.priority;
        }

        @Override
        public int getWeight() {
            return 1;
        }
    }

    public static class RolePriority extends Priority {

        private final String name;
        private final int weight;

        public RolePriority(String name, int weight) {
            this.name = name;
            this.weight = weight;
        }

        @Override
        public String getName() {
            return this.name;
        }

        @Override
        public int getPriority() {
            return 3;
        }

        @Override
        public int getWeight() {
            return this.weight;
        }
    }

}
