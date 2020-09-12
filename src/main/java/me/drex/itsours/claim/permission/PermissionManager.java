package me.drex.itsours.claim.permission;

import net.minecraft.nbt.CompoundTag;

public class PermissionManager {

    public PermissionMap settings = new PermissionMap();

    public PermissionManager(CompoundTag tag) {
        fromNBT(tag);
    }

    private void fromNBT(CompoundTag tag) {

    }

    public CompoundTag toNBT() {
        //TODO: Implement
        return new CompoundTag();
    }

    public PermissionManager() {
    }

}
