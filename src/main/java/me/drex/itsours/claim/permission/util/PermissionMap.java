package me.drex.itsours.claim.permission.util;

import me.drex.itsours.claim.permission.util.node.AbstractNode;
import me.drex.itsours.util.Color;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.minecraft.nbt.CompoundTag;

import java.util.HashMap;
import java.util.concurrent.atomic.AtomicReference;

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

    public boolean getPermission(String perm) {
        String[] nodes = perm.split("\\.");
        if (this.containsKey(perm)) return this.get(perm);
        for (Permission permission : Permission.permissions) {
            if (perm.startsWith(permission.id)) {
                return checkPermission(nodes, permission, 0, s -> {}).value;
            }
        }
        return false;
    }

    private Permission.Value checkPermission(String[] nodes, Permission permission, int i, StringInterface stringInterface) {
        //place.white_shulker_box
        //-1   .    0           (groups) length = 1
        //0    .    1           (nodes) length = 2
        //i = 0
        for (AbstractNode abstractNode : permission.groups[i].list) {
            if (abstractNode.contains(nodes[i + 1])) {
                String[] clone = nodes.clone();
                clone[i + 1] = abstractNode.getID();
                // 2 == 1 + 1
                if (nodes.length == i + 2) {
                    String perm = toPermission(nodes);
                    String perm2 = toPermission(clone);
                    System.out.println("Checking permission " + perm);
                    System.out.println("Checking permission " + perm2);
                    if (this.containsKey(perm)) {
                        stringInterface.apply(perm);
                        return Permission.Value.of(this.get(perm));
                    }
                    if (this.containsKey(perm2)) {
                        stringInterface.apply(perm);
                        return Permission.Value.of(this.get(perm2));
                    }
                } else {
                    checkPermission(nodes, permission, i + 1, stringInterface);
                    checkPermission(clone, permission, i + 1, stringInterface);
                }
            }
        }
        return Permission.Value.UNSET;
    }

    public interface StringInterface {
        void apply(String s);
    }

    private String toPermission(String... nodes) {
        StringBuilder s = new StringBuilder();
        for (String node : nodes) {
            s.append(node).append(".");
        }
        return s.substring(0, s.length() - 1);
    }

//    private boolean isSet(String... nodes) {
//        StringBuilder s = new StringBuilder();
//        for (String node : nodes) {
//            s.append(node).append(".");
//        }
//        return this.containsKey(s.substring(0, s.length() - 1));
//    }

    public void setPermission(String permission, boolean value) {
        this.put(permission, value);
    }

    public void resetPermission(String permission) {
        this.remove(permission);
    }

    private String isPermSet(String perm) {
        AtomicReference<String> p = new AtomicReference<>("");
        System.out.println("Checking if permission is set " + perm);
        String[] nodes = perm.split("\\.");
        if (this.containsKey(perm)) return perm;
        if (nodes.length == 1) return "";
        for (Permission permission : Permission.permissions) {
            if (perm.startsWith(permission.id)) {
                if (checkPermission(nodes, permission, 0, s -> {
                    System.out.println("Permission is set " + s);
                    p.set(s);
                }) != Permission.Value.UNSET) {
                    return p.get();
                }
            }
        }
        return p.get();
    }

    public boolean isPermissionSet(String permission) {
        return !isPermSet(permission).equals("");
    }

    public Component toText() {
        TextComponent.Builder text = Component.text();
        for (Entry<String, Boolean> entry : this.entrySet()) {
            text.append(Component.text(entry.getKey()).color(entry.getValue() ? Color.LIGHT_GREEN : Color.RED).append(Component.text(" ")));
        }
        return text.build();
    }

}
