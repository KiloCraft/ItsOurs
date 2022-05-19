package me.drex.itsours.claim.permission.roles;

import me.drex.itsours.claim.permission.rework.PermissionStorage;
import net.minecraft.nbt.NbtCompound;

public class Role {

    public static final Role TRUSTED = new Role();
    public static final Role DEFAULT = new Role();

    private final PermissionStorage permissions = PermissionStorage.storage();

    public Role() {
        this(new NbtCompound());
    }

    public Role(NbtCompound tag) {
        this.load(tag);
    }

    public void load(NbtCompound tag) {
        permissions.load(tag);
    }

    public NbtCompound save() {
        return permissions.save();
    }

    public PermissionStorage permissions() {
        return this.permissions;
    }
}
