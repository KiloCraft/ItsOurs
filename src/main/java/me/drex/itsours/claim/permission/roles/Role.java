package me.drex.itsours.claim.permission.roles;

import me.drex.itsours.claim.permission.util.PermissionMap;
import net.minecraft.nbt.NbtCompound;

public class Role {

    private final PermissionMap permissions_new = new PermissionMap(new NbtCompound());

    public Role(NbtCompound tag) {
        this.fromNBT(tag);
    }

    public void fromNBT(NbtCompound tag) {
        permissions_new.fromNBT(tag);
    }

    public NbtCompound toNBT() {
        return permissions_new.toNBT();
    }

    public PermissionMap permissions_new() {
        return this.permissions_new;
    }
}
