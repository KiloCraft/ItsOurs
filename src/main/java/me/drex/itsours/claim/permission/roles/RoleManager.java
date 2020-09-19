package me.drex.itsours.claim.permission.roles;

import net.minecraft.nbt.CompoundTag;

import java.util.HashMap;
import java.util.concurrent.atomic.AtomicReference;

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

    public String getRoleID(Role role) {
        AtomicReference<String> roleID = new AtomicReference<>();
        this.forEach((id, role1) -> {
            if (role1.equals(role)) {
                roleID.set(id);
            }
        });
        return roleID.get();
    }

}
