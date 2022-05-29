package me.drex.itsours.claim.permission.roles;

import me.drex.itsours.claim.permission.holder.PermissionHolder;
import net.minecraft.nbt.NbtCompound;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class Role implements Comparable<Role> {

    private final String id;
    private final PermissionHolder permissions;

    public Role(String id, PermissionHolder permissions) {
        this.id = id;
        this.permissions = permissions;
    }

    public Role(String id, NbtCompound tag) {
        this(id, PermissionHolder.storage());
        this.load(tag);
    }

    public void load(NbtCompound tag) {
        permissions.load(tag);
    }

    public String getId() {
        return id;
    }

    public NbtCompound save() {
        return permissions.save();
    }

    public PermissionHolder permissions() {
        return this.permissions;
    }

    @Override
    public int compareTo(@NotNull Role other) {
        List<Role> orderedRoles = RoleManager.INSTANCE.getOrderedRoles();
        // Compare index in role orders (lower is better)
        return Integer.compare(orderedRoles.indexOf(other), orderedRoles.indexOf(this));
    }

}
