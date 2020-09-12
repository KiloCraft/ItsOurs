package me.drex.itsours.claim.permission;

import me.drex.itsours.claim.permission.util.PermissionMap;
import net.minecraft.nbt.CompoundTag;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PermissionManager {

    public PermissionMap settings = new PermissionMap();
    public Map<UUID, PermissionMap> playerPermission = new HashMap<>();

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
