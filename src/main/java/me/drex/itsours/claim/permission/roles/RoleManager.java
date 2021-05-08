package me.drex.itsours.claim.permission.roles;

import java.util.HashMap;
import net.minecraft.nbt.NbtCompound;

public class RoleManager extends HashMap<String, Role> {

    public RoleManager(NbtCompound tag) {
        fromNBT(tag);
    }

    public void fromNBT(NbtCompound tag) {
        tag.getKeys().forEach(id -> this.put(id, new Role(tag.getCompound(id))));
        if (!this.containsKey("trusted")) {
            this.put("trusted", new Role(new NbtCompound()));
        }
        if (!this.containsKey("default")) {
            this.put("default", new Role(new NbtCompound()));
        }
    }

    public NbtCompound toNBT() {
        NbtCompound tag = new NbtCompound();
        this.forEach((id, role) -> tag.put(id, role.toNBT()));
        return tag;
    }

    public String getRoleID(Role role) {
        for (Entry<String, Role> entry : entrySet()) {
            if (role.equals(entry.getValue())) return entry.getKey();
        }
        return null;
    }

    public Role getRole(String id) {
        for (Entry<String, Role> entry : entrySet()) {
            if (entry.getKey().equals(id)) return entry.getValue();
        }
        return null;
    }

}
