package me.drex.itsours.claim.permission.util;

import me.drex.itsours.claim.permission.util.node.AbstractNode;
import me.drex.itsours.util.Color;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.minecraft.nbt.NbtCompound;
import java.util.HashMap;

public class PermissionMap extends HashMap<String, Boolean> {

    public PermissionMap(NbtCompound tag) {
        fromNBT(tag);
    }

    public void fromNBT(NbtCompound tag) {
        tag.getKeys().forEach(permission -> this.put(permission, tag.getBoolean(permission)));
    }

    public NbtCompound toNBT() {
        NbtCompound tag = new NbtCompound();
        this.forEach(tag::putBoolean);
        return tag;
    }

    public Permission.Value getPermission(String permission) {
        return getPermission(permission, s -> {});
    }

    public Permission.Value getPermission(String permission, PermissionCallback c) {
        if (this.containsKey(permission)) return Permission.Value.of(this.get(permission));
        String[] nodes = permission.split("\\.");
        Permission perm = Permission.getPermission(permission);
        if (perm != null && nodes.length == 1) return perm.getDefaultValue();
        if (perm == null) return Permission.Value.UNSET;
        return checkPermission(nodes, perm, 0, c);
    }

    private Permission.Value checkPermission(String[] nodes, Permission permission, int i, PermissionCallback c) {
        for (AbstractNode abstractNode : permission.groups[i].list) {
            if (abstractNode.contains(nodes[i + 1])) {
                String[] clone = nodes.clone();
                clone[i + 1] = abstractNode.getID();
                if (nodes.length == i + 2) {
                    String perm = toPermission(nodes);
                    String perm2 = toPermission(clone);
                    if (this.containsKey(perm)) {
                        c.apply(perm);
                        return Permission.Value.of(this.get(perm));
                    }
                    if (this.containsKey(perm2)) {
                        c.apply(perm2);
                        return Permission.Value.of(this.get(perm2));
                    }
                } else {
                    checkPermission(nodes, permission, i + 1, c);
                    checkPermission(clone, permission, i + 1, c);
                }
            }
        }
        return Permission.Value.UNSET;
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
