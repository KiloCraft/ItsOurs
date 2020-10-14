package me.drex.itsours.claim.permission.util;

import me.drex.itsours.claim.permission.util.node.AbstractNode;
import me.drex.itsours.util.Color;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.minecraft.block.Block;
import net.minecraft.entity.EntityType;
import net.minecraft.item.Item;
import net.minecraft.util.registry.Registry;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class Permission {

    public static final Pattern PERMISSION = Pattern.compile("[\\w]+(\\.[\\w]+)?");
    public static List<Permission> permissions = new ArrayList<>();
    public static Permission PLACE = new Permission("place", Group.BLOCK);
    public final String id;
    public final Group[] groups;

    Permission(String id, Group... groups) {
        permissions.add(this);
        this.id = id;
        this.groups = groups;
    }

    public static boolean isValid(String permission) {
        if (!PERMISSION.matcher(permission).matches()) return false;
        String[] nodes = permission.split("\\.");
        boolean[] b = new boolean[nodes.length - 1];
        Permission p = Permission.getPermission(permission);
        if (p == null) return false;
        for (int i = 0; i < nodes.length - 1; i++) {
            for (AbstractNode abstractNode : p.groups[i].list) {
                if (abstractNode.getID().equals(nodes[i + 1])) b[i] = true;
            }
        }
        for (boolean val : b) {
            if (!val) return false;
        }
        return true;
    }

    public static Permission getPermission(String perm) {
        for (Permission permission : Permission.permissions) {
            if (perm.startsWith(permission.id)) {
                return permission;
            }
        }
        return null;
    }

    public static String toString(Block block) {
        return Registry.BLOCK.getId(block).getPath();
    }

    public static String toString(EntityType entityType) {
        return Registry.ENTITY_TYPE.getId(entityType).getPath();
    }

    public static String toString(Item item) {
        return Registry.ITEM.getId(item).getPath();
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
