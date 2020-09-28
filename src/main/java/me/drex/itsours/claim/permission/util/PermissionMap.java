package me.drex.itsours.claim.permission.util;

import net.minecraft.nbt.CompoundTag;

import java.util.HashMap;

public class PermissionMap extends HashMap<String, Boolean> {

    public PermissionMap(CompoundTag tag) {
        fromNBT(tag);
    }

    public void fromNBT(CompoundTag tag) {
        tag.getKeys().forEach(permission -> this.put(permission, tag.getBoolean(permission)));
    }

    public CompoundTag toNBT() {
        CompoundTag tag = new CompoundTag();
        this.forEach(tag::putBoolean);
        return tag;
    }

    public boolean getPermission(String permission) {
        return this.get(permission);
    }

    public void setPermission(String permission, boolean value) {
        this.put(permission, value);
    }

    public boolean isPermissionSet(String permission) {
        return this.containsKey(permission);
    }

}
