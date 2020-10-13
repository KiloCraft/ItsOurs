package me.drex.itsours.claim.permission.util;

import me.drex.itsours.claim.permission.util.node.AbstractNode;
import net.minecraft.block.Block;
import net.minecraft.entity.EntityType;
import net.minecraft.item.Item;
import net.minecraft.util.registry.Registry;

import java.util.ArrayList;
import java.util.List;

public class Permission {

    public final String id;
    public final Group[] groups;

    public static List<Permission> permissions = new ArrayList<>();

    Permission(String id, Group... groups) {
        permissions.add(this);
        this.id = id;
        this.groups = groups;
    }

    public static boolean isValid(String perm) {
        String[] nodes = perm.split("\\.");
        boolean[] b = new boolean[nodes.length];
        for (Permission permission : Permission.permissions) {
            if (perm.startsWith(permission.id)) {
                for (int i = 0; i < nodes.length - 2; i++) {
                    for (AbstractNode abstractNode : permission.groups[i].list) {
                        if (abstractNode.contains(nodes[i + 2])) {
                            b[i + 2] = true;
                        }
                    }
                }
            }
        }
        for (boolean val : b) {
            if (!val) return false;
        }
        return true;
    }

    public static final Permission PLACE = new Permission("place", Group.BLOCK);

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
        TRUE(true),
        FALSE(false),
        UNSET(false);

        public final boolean value;
        Value(boolean value) {
            this.value = value;
        }

        public static Value of(boolean value) {
            return value ? TRUE : FALSE;
        }
    }

}
