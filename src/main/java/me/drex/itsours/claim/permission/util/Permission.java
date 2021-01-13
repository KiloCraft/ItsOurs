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
import java.util.Optional;
import java.util.regex.Pattern;

public class Permission {

    public static final Pattern PERMISSION = Pattern.compile("[\\w]+(\\.[\\w]+)*");
    public static final List<Permission> permissions = new ArrayList<>();
<<<<<<< Updated upstream
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
    public static final Setting KEEP_INVENTORY = new Setting("keepinventory", Value.TRUE);
    public final String id;
    public final Group[] groups;
=======
    public static final Permission PLACE = new Permission("place",  Group.BLOCK).desc("Place blocks");
    public static final Permission MINE = new Permission("mine",  Group.BLOCK, Group.ITEMS).desc("Mine blocks");
    public static final Permission INTERACT_BLOCK = new Permission("interact_block", Group.INTERACTABLE_BLOCKS).desc("Rightclick on blocks");
    public static final Permission USE_ON_BLOCK = new Permission("use_on_block", Group.USE_ON_BLOCKS, Group.BLOCK).desc("Use a specific item on a block");
    public static final Permission USE_ITEM = new Permission("use_item", Group.USE_ITEM).desc("Rightclick with an item");
    public static final Permission DAMAGE_ENTITY = new Permission("damage_entity", Group.ENTITY).desc("Hit / damage entities");
    public static final Permission INTERACT_ENTITY = new Permission("interact_entity", Group.ENTITY).desc("Rightclick on entities");
    public static final Permission MODIFY = new Permission("modify", Group.MODIFY).desc("Claim permissions");
    public static final Permission PVP = new Setting("pvp").desc("Toggle Player vs Player");
    public static final Permission MOBSPAWN = new Setting("mobspawn").desc("Toggle mobspawning").setting();
    public static final Permission EXPLOSIONS = new Setting("explosions").desc("Toggle explosion block damage").setting();
    public static final Permission FLUID_CROSSES_BORDERS = new Setting("fluid_crosses_borders").desc("Toggle fluids crossing claim borders").setting();
    public final String id;
    public final Group[] groups;
    public String information;
    private Optional<String> description = Optional.empty();
    private boolean global = false;
    private boolean setting = false;
>>>>>>> Stashed changes
    public Value defaultValue = Value.UNSET;

    Permission(String id, Group... groups) {
        permissions.add(this);
        this.id = id;
        this.groups = groups;
    }

    Permission(String id, Group... groups) {
        this.id = id;
        this.groups = groups;
    }

    public Permission desc(String description) {
        this.description = Optional.of(description);
        return this;
    }

    public Permission setting() {
        this.setting = true;
        return this;
    }

    public boolean isSetting() {
        return this.setting;
    }

    public static boolean isValid(String permission) {
        if (!PERMISSION.matcher(permission).matches()) return false;
        String[] nodes = permission.split("\\.");
        boolean[] b = new boolean[nodes.length - 1];
        Permission p = Permission.getPermission(permission);
        if (p == null) return false;
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
            if (perm.split("\\.")[0].equals(permission.id)) {
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

<<<<<<< Updated upstream
=======
    public Value getDefaultValue() {
        return this.defaultValue;
    }

    public boolean isGlobal() {
        return global;
    }

    public Permission setGlobal(boolean value) {
        this.global = value;
        return this;
    }

>>>>>>> Stashed changes

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
