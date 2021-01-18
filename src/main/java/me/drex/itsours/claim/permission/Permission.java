package me.drex.itsours.claim.permission;

import me.drex.itsours.claim.permission.util.newNode.util.InvalidPermissionException;
import me.drex.itsours.claim.permission.util.newNode.util.Node;
import me.drex.itsours.util.Color;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;

import java.util.List;
import java.util.Optional;

public class Permission {

    private final List<Node> nodes;

    public Permission(List<Node> nodes) {
        this.nodes = nodes;
    }

    public static Optional<Permission> of(String permission) {
        try {
            return Optional.of(new Permission(PermissionList.permission.getNodes(permission)));
        } catch (InvalidPermissionException e) {
            return Optional.empty();
        }
    }

    public List<Node> getNodes() {
        return nodes;
    }

    public int nodes() {
        return nodes.size();
    }

    public Permission up(int amount) {
        return new Permission(nodes.subList())
    }

    public String asString() {
        StringBuilder result = new StringBuilder();
        for (int i = nodes.size() - 1; i >= 0; i--) {
            result.append(nodes.get(i)).append(i == (nodes.size() - 1) ? "" : ".");
        }
        return result.toString();
    }

    public Permission.Value getDefaultValue() {
        return nodes.get(nodes.size() - 1).getDefaultValue();
    }

    public enum Value {
        TRUE(true, "true", Color.GREEN),
        FALSE(false, "false", Color.RED),
        UNSET(false, "unset", Color.GRAY);

        public final boolean value;
        public final String name;
        public final TextColor color;

        Value(boolean value, String name, TextColor color) {
            this.value = value;
            this.name = name;
            this.color = color;
        }

        public static Value of(boolean value) {
            return value ? TRUE : FALSE;
        }

        public Component format() {
            return Component.text(this.name).color(this.color);
        }
    }

}
