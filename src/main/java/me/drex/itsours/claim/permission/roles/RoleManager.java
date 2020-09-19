package me.drex.itsours.claim.permission.roles;

import net.minecraft.nbt.CompoundTag;

import java.util.HashMap;

public class RoleManager extends HashMap<String, Role> {

    public RoleManager(CompoundTag tag) {

    }

    public void fromNBT(CompoundTag tag) {
        tag.getKeys().forEach(id -> this.put(id, new Role(tag.getCompound(id))));
    }

    public CompoundTag toNBT() {
        CompoundTag tag = new CompoundTag();
        this.forEach((id, role) -> tag.put(id, role.toNBT()));
        return tag;
    }

}
