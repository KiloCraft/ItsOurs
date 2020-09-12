package me.drex.itsours.claim.permission.roles;

import net.minecraft.nbt.CompoundTag;

import java.util.HashMap;
import java.util.Map;

public class RoleManager {

    Map<String, Role> roles = new HashMap<>();

    public RoleManager(CompoundTag tag) {

    }

    public void fromNBT(CompoundTag tag) {
        tag.getKeys().forEach(id -> roles.put(id, new Role(tag.getCompound(id))));
    }

    public CompoundTag toNBT() {
        CompoundTag tag = new CompoundTag();
        roles.forEach((id, role) -> tag.put(id, role.toNBT()));
        return tag;
    }

}
