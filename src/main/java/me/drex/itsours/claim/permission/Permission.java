package me.drex.itsours.claim.permission;

import me.drex.itsours.claim.permission.util.node.util.InvalidPermissionException;
import me.drex.itsours.claim.permission.util.node.util.Node;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.List;
import java.util.Optional;

public class Permission {

    private final List<Node> nodes;

    public Permission(List<Node> nodes) {
        this.nodes = nodes;
    }

    public static Optional<Permission> permission(String permission) {
        try {
            return Optional.of(new Permission(PermissionList.INSTANCE.permission.getNodes(permission)));
        } catch (InvalidPermissionException e) {
            return Optional.empty();
        }
    }

    public static Optional<Permission> setting(String setting) {
        try {
            return Optional.of(new Permission(PermissionList.INSTANCE.setting.getNodes(setting)));
        } catch (InvalidPermissionException e) {
            try {
                return Optional.of(new Permission(PermissionList.INSTANCE.permission.getNodes(setting)));
            } catch (InvalidPermissionException e1) {
                return Optional.empty();
            }
        }
    }

    public Permission up() {
        return new Permission(nodes.subList(1, nodes.size()));
    }

    public List<Node> getNodes() {
        return nodes;
    }

    public int nodes() {
        return nodes.size();
    }

    public String asString() {
        StringBuilder result = new StringBuilder();
        for (int i = nodes.size() - 1; i >= 0; i--) {
            result.append(nodes.get(i).getId()).append(i == 0 ? "" : ".");
        }
        return result.toString();
    }

    public String asString(String replacement, int replacementIndex) {
        StringBuilder result = new StringBuilder();
        for (int i = nodes.size() - 1; i >= 0; i--) {
            result.append(i != replacementIndex ? nodes.get(i).getId() : replacement).append(i == 0 ? "" : ".");
        }
        return result.toString();
    }

    @Override
    public int hashCode() {
        return this.asString().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Permission other) {
            return other.asString().equals(this.asString());
        } else {
            return false;
        }
    }

}
