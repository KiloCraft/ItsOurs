package me.drex.itsours.claim.permission.roles;

import net.minecraft.nbt.CompoundTag;

import java.util.HashMap;

public class RoleManager extends HashMap<String, Role> {

    public RoleManager(CompoundTag tag) {
        fromNBT(tag);
    }

    public void fromNBT(CompoundTag tag) {
        tag.getKeys().forEach(id -> this.put(id, new Role(tag.getCompound(id))));
        if (!this.containsKey("trusted")) {
            this.put("trusted", new Role(new CompoundTag()));
        }
        if (!this.containsKey("default")) {
            this.put("default", new Role(new CompoundTag()));
        }
    }

    public CompoundTag toNBT() {
        CompoundTag tag = new CompoundTag();
        this.forEach((id, role) -> tag.put(id, role.toNBT()));
        return tag;
    }

    public String getRoleID(Role role) {
        for (Entry<String, Role> entry : entrySet()) {
            if (role.equals(entry.getValue())) return entry.getKey();
        }
        return null;
    }

}
