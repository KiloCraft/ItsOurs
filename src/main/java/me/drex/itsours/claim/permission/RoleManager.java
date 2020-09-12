package me.drex.itsours.claim.permission;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RoleManager {

    Map<String, Role> roles = new HashMap<>();

    public RoleManager(CompoundTag tag) {

    }

    public void fromNBT(CompoundTag tag) {
        tag.getKeys().forEach(id -> roles.put(id, new Role((CompoundTag) tag.get(id))));
    }

    public CompoundTag toNBT() {
        CompoundTag tag = new CompoundTag();
        roles.forEach((id, role) -> tag.put(id, role.toNBT()));
        return tag;
    }

}
