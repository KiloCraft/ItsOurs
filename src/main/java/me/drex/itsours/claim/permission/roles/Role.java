package me.drex.itsours.claim.permission.roles;

import me.drex.itsours.claim.permission.util.PermissionMap;
import net.minecraft.nbt.CompoundTag;

public class Role {

    private PermissionMap permissions;

    public Role (CompoundTag tag) {
        this.fromNBT(tag);
    }

    public void fromNBT(CompoundTag tag) {
        permissions.fromNBT(tag);
    }

    public CompoundTag toNBT() {
        return permissions.toNBT();
    }

    public PermissionMap permissions() {
        return this.permissions;
    }
}
