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

    public static final Pattern PERMISSION = Pattern.compile("[\\w]+(\\.[\\w]+)*");
    public static final List<Permission> permissions = new ArrayList<>();
    public static final Permission PLACE = new Permission("place", Group.BLOCK);
    public static final Permission MINE = new Permission("mine", Group.BLOCK, Group.ITEMS);
    public static final Permission INTERACT_BLOCK = new Permission("interact_block", Group.INTERACTABLE_BLOCKS);
    public static final Permission USE_ON_BLOCK = new Permission("use_on_block", Group.USE_ON_BLOCKS, Group.BLOCK);
    public static final Permission USE_ITEM = new Permission("use_item", Group.USE_ITEM);
    public static final Permission DAMAGE_ENTITY = new Permission("damage_entity", Group.ENTITY);
    public static final Permission INTERACT_ENTITY = new Permission("interact_entity", Group.ENTITY);
    public static final Permission MODIFY = new Permission("modify", Group.MODIFY);
    public static final Setting PVP = new Setting("pvp");
    public static final Setting MOBSPAWN = new Setting("mobspawn");
    public static final Setting EXPLOSIONS = new Setting("explosions");
    public final String id;
    public final Group[] groups;
    public Value defaultValue = Value.UNSET;

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
        if (p == null || (p instanceof Setting)) return false;
        for (int i = 0; i < nodes.length - 1; i++) {
            for (AbstractNode abstractNode : p.groups[i].list) {
                if (abstractNode.getID().equals(nodes[i + 1])) {
                    b[i] = true;
                    break;
                }
            }
        }
        for (boolean val : b) {
            if (!val) return false;
        }
        return true;
    }

    public static Permission getPermission(String perm) {
        for (Permission permission : Permission.permissions) {
            if (perm
                    .split("\\.")
                    [0]
                    .equals(permission.id)) {
                return permission;
            }
        }
        return null;
    }

    public Value getDefaultValue() {
        return this.defaultValue;
    }

    public static String toString(Block block) {
        return Registry.BLOCK.getId(block).getPath();
    }

    public static String toString(EntityType<?> entityType) {
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
