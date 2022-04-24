package me.drex.itsours.claim.permission.roles;

import it.unimi.dsi.fastutil.objects.Object2IntArrayMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import me.drex.itsours.ItsOurs;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;

import java.util.ArrayList;
import java.util.List;

public class PlayerRoleManager {

    private final List<Role> removed = new ArrayList<>();
    private final Object2IntMap<Role> roles = new Object2IntArrayMap<>();

    public PlayerRoleManager(NbtCompound tag) {
        fromNBT(tag);
    }

    public Object2IntMap<Role> getRoles() {
        return roles;
    }

    public List<Role> getRemoved() {
        return removed;
    }

    public boolean addRole(Role role, int weight) {
        boolean changed = false;
        if (!roles.containsKey(role)) {
            roles.put(role, weight);
            changed = true;
        }
        if (removed.contains(role)) {
            removed.remove(role);
            changed = true;
        }
        return changed;
    }

    public boolean removeRole(Role role) {
        boolean changed = false;
        if (roles.containsKey(role)) {
            roles.remove(role);
            changed = true;
        }
        if (!removed.contains(role)) {
            removed.add(role);
            changed = true;
        }
        return changed;
    }

    public boolean unsetRole(Role role) {
        boolean changed = false;
        if (roles.containsKey(role)) {
            roles.remove(role);
            changed = true;
        }
        if (removed.contains(role)) {
            removed.remove(role);
            changed = true;
        }
        return changed;
    }

    public void fromNBT(NbtCompound tag) {
        int dataVersion = ItsOurs.INSTANCE.getDataVersion();
        if (dataVersion == 0) {
            tag.getKeys().forEach(roleID -> {
                int weight = tag.getInt(roleID);
                Role role = ItsOurs.INSTANCE.getRoleManager().getRole(roleID);
                if (role != null) {
                    roles.put(role, weight);
                }
            });
        } else if (dataVersion > 0) {
            if (tag.contains("removed")) {
                NbtList nbtList = tag.getList("removed", 8);
                for (NbtElement nbtElement : nbtList) {
                    if (nbtElement instanceof NbtString) {
                        NbtString nbtString = (NbtString) nbtElement;
                        Role role = ItsOurs.INSTANCE.getRoleManager().get(nbtString.asString());
                        if (role != null) {
                            removed.add(role);
                        }
                    }
                }
            }
            if (tag.contains("added")) {
                NbtCompound nbtCompound = tag.getCompound("added");
                for (String key : nbtCompound.getKeys()) {
                    int weight = nbtCompound.getInt(key);
                    Role role = ItsOurs.INSTANCE.getRoleManager().get(key);
                    if (role != null) {
                        roles.put(role, weight);
                    }
                }
            }
        }
    }

    public NbtCompound toNBT() {
        NbtCompound tag = new NbtCompound();
        if (ItsOurs.INSTANCE.getDataVersion() >= 0) {
            if (roles.size() > 0) {
                NbtCompound nbtCompound = new NbtCompound();
                for (Object2IntMap.Entry<Role> entry : roles.object2IntEntrySet()) {
                    nbtCompound.putInt(ItsOurs.INSTANCE.getRoleManager().getRoleID(entry.getKey()), entry.getIntValue());
                }
                tag.put("added", nbtCompound);
            }
            if (removed.size() > 0) {
                NbtList nbtList = new NbtList();
                for (Role role : removed) {
                    nbtList.add(NbtString.of(ItsOurs.INSTANCE.getRoleManager().getRoleID(role)));
                }
                tag.put("removed", nbtList);
            }
        }
        return tag;
    }

}
