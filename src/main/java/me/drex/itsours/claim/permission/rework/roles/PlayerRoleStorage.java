package me.drex.itsours.claim.permission.rework.roles;

import it.unimi.dsi.fastutil.objects.Object2IntArrayMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import me.drex.itsours.ItsOurs;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class PlayerRoleStorage {

    private final List<RoleRework> roles = new LinkedList<>();
    private static final Logger LOGGER = ItsOurs.LOGGER;

    public PlayerRoleStorage() {
        this(null);
    }

    public PlayerRoleStorage(@Nullable NbtElement tag) {
        fromNBT(tag);
    }

    public void fromNBT(@Nullable NbtElement tag) {
        int dataVersion = ItsOurs.INSTANCE.getDataVersion();
        if (dataVersion > 1) {
            if (tag instanceof NbtList list) {
                for (NbtElement element : list) {
                    if (element instanceof NbtString nbtString) {
                        RoleRework role = RoleManagerRework.INSTANCE.getRole(nbtString.asString());
                        if (role != null) {
                            roles.add(role);
                        } else {
                            LOGGER.warn("Couldn't load unknown role {}", nbtString.asString());
                        }
                    } else {
                        throw new IllegalArgumentException("Expected nbt string, but received " + element);
                    }
                }
            } else {
                if (tag != null) throw new IllegalArgumentException("Expected nbt list, but received " + tag);
            }
        } else {
            if (tag instanceof NbtCompound nbt && nbt.contains("added")) {
                NbtCompound nbtCompound = nbt.getCompound("added");
                Object2IntMap<RoleRework> weigthedRoles = new Object2IntArrayMap<>();
                for (String key : nbtCompound.getKeys()) {
                    int weight = nbtCompound.getInt(key);
                    RoleRework role = RoleManagerRework.INSTANCE.getRole(key);
                    if (role != null) {
                        weigthedRoles.put(role, weight);
                    }
                }
                roles.addAll(weigthedRoles.object2IntEntrySet().stream().sorted(Comparator.comparingInt(Object2IntMap.Entry::getIntValue)).map(Map.Entry::getKey).toList());
            }
        }
    }

    public NbtList toNbt() {
        NbtList nbtList = new NbtList();
        for (RoleRework role : roles) {
            nbtList.add(NbtString.of(role.getId()));
        }
        return nbtList;
    }

    public List<RoleRework> getRoles() {
        return this.roles;
    }

    public boolean addRole(RoleRework role) {
        return roles.add(role);
    }

    public boolean removeRole(RoleRework role) {
        return roles.remove(role);
    }

    public int getSize() {
        return roles.size();
    }

}
