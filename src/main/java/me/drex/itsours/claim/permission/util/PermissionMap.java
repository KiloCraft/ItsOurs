package me.drex.itsours.claim.permission.util;

import me.drex.itsours.claim.permission.Permission;
import me.drex.itsours.claim.permission.util.context.PermissionContext;
import me.drex.itsours.claim.permission.util.node.util.GroupNode;
import me.drex.itsours.claim.permission.util.node.util.Node;
import me.drex.itsours.util.Color;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.minecraft.nbt.NbtCompound;

import java.util.HashMap;
import java.util.Optional;

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

    public PermissionContext getPermission(Permission permission, PermissionContext.Priority priority) {
        PermissionContext context = new PermissionContext();
        if (this.containsKey(permission.asString()))
            context.add(permission, priority, Permission.Value.of(this.get(permission.asString())));
        for (int i = permission.getNodes().size() - 1; i > 0; i--) {
            Node node = permission.getNodes().get(i);
            Node child = permission.getNodes().get(i - 1);
            for (Node childNode : node.getNodes()) {
                if (childNode instanceof GroupNode groupNode) {
                    if (groupNode.contains(child.getId())) {
                        String s = permission.asString(groupNode.getId(), i - 1);
                        if (this.containsKey(s)) {
                            Optional<Permission> optional = Permission.permission(s);
                            optional.ifPresent(perm -> context.add(perm, priority, Permission.Value.of(this.get(s))));
                        }
                    }
                }
            }
        }
        return context;
    }

    public void setPermission(String permission, Permission.Value value) {
        if (value == Permission.Value.UNSET) this.remove(permission);
        else this.put(permission, value.value);
    }

    public Permission.Value getValue(String permission) {
        Boolean bool = this.get(permission);
        if (bool != null) {
            return Permission.Value.of(bool);
        } else {
            return Permission.Value.UNSET;
        }
    }

    public Component toText() {
        TextComponent.Builder text = Component.text();
        for (Entry<String, Boolean> entry : this.entrySet()) {
            text.append(Component.text(entry.getKey()).color(entry.getValue() ? Color.LIGHT_GREEN : Color.RED).append(Component.text(" ")));
        }
        return text.build();
    }

}
