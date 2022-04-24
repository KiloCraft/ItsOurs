package me.drex.itsours.claim.permission.roles;

import me.drex.itsours.claim.permission.util.PermissionMap;
import net.minecraft.nbt.NbtCompound;

public class Role {

    public static final Role TRUSTED = new Role();
    public static final Role DEFAULT = new Role();

    private final PermissionMap permissions = new PermissionMap(new NbtCompound());

    public Role() {
        this(new NbtCompound());
    }

    public Role(NbtCompound tag) {
        this.fromNBT(tag);
    }

    public void fromNBT(NbtCompound tag) {
        permissions.fromNBT(tag);
    }

    public NbtCompound toNBT() {
        return permissions.toNBT();
    }

    public PermissionMap permissions() {
        return this.permissions;
    }
}
