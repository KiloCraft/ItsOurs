package me.drex.itsours.claim.permission.roles;

import me.drex.itsours.claim.permission.util.PermissionMap;
import net.minecraft.nbt.CompoundTag;

public class Role {

    private final PermissionMap permissions = new PermissionMap();

    public Role (CompoundTag tag) {
        this.fromNBT(tag);
    }

    public void fromNBT(CompoundTag tag) {
        tag.getKeys().forEach(permission -> permissions.put(permission, tag.getBoolean(permission)));
    }

    public CompoundTag toNBT() {
        CompoundTag tag = new CompoundTag();
        permissions.forEach(tag::putBoolean);
        return tag;
    }

}
