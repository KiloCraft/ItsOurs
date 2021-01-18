package me.drex.itsours.claim.permission.util;

import me.drex.itsours.claim.permission.Permission;
import me.drex.itsours.claim.permission.util.context.PermissionContext;
import me.drex.itsours.claim.permission.util.context.SimpleContext;
import me.drex.itsours.claim.permission.util.newNode.util.Node;
import me.drex.itsours.claim.permission.util.node.AbstractNode;
import me.drex.itsours.util.Color;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.minecraft.nbt.CompoundTag;

import java.util.HashMap;

public class PermissionMap extends HashMap<String, Boolean> {

    public PermissionMap(CompoundTag tag) {
        fromNBT(tag);
    }

    public void fromNBT(CompoundTag tag) {
        tag.getKeys().forEach(permission -> this.put(permission, tag.getBoolean(permission)));
    }

    public CompoundTag toNBT() {
        CompoundTag tag = new CompoundTag();
        this.forEach(tag::putBoolean);
        return tag;
    }

    public SimpleContext getPermission(Permission permission) {
        if (this.containsKey(permission)) return new SimpleContext(Permission.Value.of(this.get(permission)), SimpleContext.Reason.PERMISSION);
        String[] nodes = permission.asString().split("\\.");
        SimpleContext context = checkPermission(nodes, permission.getNodes().get(0), 0);
        if (context.getValue() != Permission.Value.UNSET) return context;
        return new SimpleContext(permission.getDefaultValue(), SimpleContext.Reason.DEFAULT);
    }

    private SimpleContext checkPermission(String[] nodes, Node permission, int i) {
        for (Node node : permission.getNodes()) {
            if (node.contains(nodes[i + 1])) {
                String[] clone = nodes.clone();
                clone[i + 1] = node.getId();
                if (nodes.length == i + 2) {
                    String perm = toPermission(nodes);
                    String perm2 = toPermission(clone);
                    if (this.containsKey(perm)) {
                        //TODO: Add permission that succeeded
                        return new SimpleContext(Permission.Value.of(this.get(perm)), SimpleContext.Reason.PERMISSION);
                    }
                    if (this.containsKey(perm2)) {
                        return new SimpleContext(Permission.Value.of(this.get(perm)), SimpleContext.Reason.PERMISSION);
                    }
                } else {
                    checkPermission(nodes, node, i + 1);
                    checkPermission(clone, node, i + 1);
                }
            }
        }
        return new SimpleContext();
    }

    private String toPermission(String... nodes) {
        StringBuilder s = new StringBuilder();
        for (String node : nodes) {
            s.append(node).append(".");
        }
        return s.substring(0, s.length() - 1);
    }

    public void setPermission(String permission, Permission.Value value) {
        if (value == Permission.Value.UNSET) this.remove(permission);
        else this.put(permission, value.value);
    }

    public void resetPermission(String permission) {
        this.remove(permission);
    }

    public Component toText() {
        TextComponent.Builder text = Component.text();
        for (Entry<String, Boolean> entry : this.entrySet()) {
            text.append(Component.text(entry.getKey()).color(entry.getValue() ? Color.LIGHT_GREEN : Color.RED).append(Component.text(" ")));
        }
        return text.build();
    }

    public interface PermissionCallback {
        void apply(String s);
    }

}
