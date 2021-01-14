package me.drex.itsours.claim.permission;

import com.google.common.collect.Maps;
import me.drex.itsours.ItsOursMod;
import me.drex.itsours.claim.permission.roles.Role;
import me.drex.itsours.claim.permission.util.Permission;
import me.drex.itsours.claim.permission.util.PermissionMap;
import me.drex.itsours.claim.permission.util.context.PermissionContext;
import net.minecraft.nbt.CompoundTag;

import java.util.*;

import static me.drex.itsours.claim.permission.util.Permission.Value.UNSET;

public class PermissionManager {
    public PermissionMap settings = new PermissionMap(new CompoundTag());
    public HashMap<UUID, PermissionMap> playerPermission = Maps.newHashMap();
    public HashMap<UUID, HashMap<Role, Integer>> roles = Maps.newHashMap();

    public PermissionManager(CompoundTag tag) {
        fromNBT(tag);
    }

    private void fromNBT(CompoundTag tag) {
        if (tag.contains("settings")) settings.fromNBT(tag.getCompound("settings"));
        if (tag.contains("players")) {
            CompoundTag players = tag.getCompound("players");
            players.getKeys().forEach(uuid -> {
                CompoundTag player = players.getCompound(uuid);
                if (player.contains("permission"))
                    playerPermission.put(UUID.fromString(uuid), new PermissionMap(player.getCompound("permission")));
                if (player.contains("role")) {
                    CompoundTag roleTag = player.getCompound("role");
                    HashMap<Role, Integer> roleWeight = new HashMap<>();
                    roleTag.getKeys().forEach(roleID -> {
                        int weight = roleTag.getInt(roleID);
                        Role role = ItsOursMod.INSTANCE.getRoleManager().get(roleID);
                        roleWeight.put(role, weight);
                    });
                    roles.put(UUID.fromString(uuid), roleWeight);
                }
            });
        }
    }

    public CompoundTag toNBT() {
        CompoundTag tag = new CompoundTag();
        CompoundTag players = new CompoundTag();

        List<UUID> uuidSet = new ArrayList<>();
        uuidSet.addAll(playerPermission.keySet());
        uuidSet.addAll(roles.keySet());
        for (UUID uuid : uuidSet) {
            CompoundTag player = new CompoundTag();
            PermissionMap pm = playerPermission.get(uuid);
            if (pm != null) {
                player.put("permission", pm.toNBT());
            }
            HashMap<Role, Integer> roleMap = roles.get(uuid);
            if (roleMap != null) {
                CompoundTag roleTag = new CompoundTag();
                roleMap.forEach((role, integer) -> {
                    roleTag.putInt(ItsOursMod.INSTANCE.getRoleManager().getRoleID(role), integer);
                });
                player.put("role", roleTag);
            }
            players.put(uuid.toString(), player);
        }

        if (!settings.isEmpty()) tag.put("settings", settings.toNBT());
        tag.put("players", players);
        return tag;
    }

    public void addRole(UUID uuid, Role role, int weight) {
        HashMap<Role, Integer> roleWeight = this.roles.get(uuid);
        if (roleWeight == null) roleWeight = new HashMap<>();
        roleWeight.put(role, weight);
        this.roles.put(uuid, roleWeight);
    }

    public void removeRole(UUID uuid, Role role) {
        HashMap<Role, Integer> roleWeight = this.roles.get(uuid);
        roleWeight.remove(role);
        this.roles.put(uuid, roleWeight);
    }

    public HashMap<Role, Integer> getRolesByWeight(UUID uuid) {
        HashMap<Role, Integer> sortedRoles;
        roles.computeIfAbsent(uuid, k -> new HashMap<>());
        sortedRoles = sort(roles.get(uuid));
        return sortedRoles;
    }

    public Permission.Value hasPermission(UUID uuid, String permission) {
        Permission.Value value = UNSET;
        Permission.Value v1 = settings.getPermission(permission);
        if (v1 != UNSET)
            value = v1;
        for (Role role : this.getRolesByWeight(uuid).keySet()) {
            Permission.Value v2 = role.permissions().getPermission(permission);
            if (v2 != UNSET)
                value = v2;
        }
        if (playerPermission.get(uuid) != null) {
            Permission.Value v3 = playerPermission.get(uuid).getPermission(permission);
            if (v3 != UNSET)
                value = v3;
        }
        if (value == UNSET && permission.contains(".")) {
            String[] node = permission.split("\\.");
            return hasPermission(uuid, permission.substring(0, (permission.length() - (node[node.length - 1]).length() - 1)));
        }
        return value;
    }

    public PermissionContext hasPermission(UUID uuid, Permission permission) {
        PermissionContext context = new PermissionContext();
        Permission.Value v1 = settings.getPermission(permission);
        if (v1 != UNSET)
            value = v1;
        for (Role role : this.getRolesByWeight(uuid).keySet()) {
            Permission.Value v2 = role.permissions().getPermission(permission);
            if (v2 != UNSET)
                value = v2;
        }
        if (playerPermission.get(uuid) != null) {
            Permission.Value v3 = playerPermission.get(uuid).getPermission(permission);
            if (v3 != UNSET)
                value = v3;
        }
        if (value == UNSET && permission.contains(".")) {
            String[] node = permission.split("\\.");
            return hasPermission(uuid, permission.substring(0, (permission.length() - (node[node.length - 1]).length() - 1)));
        }
        return value;
    }

    public boolean hasRole(UUID uuid, Role role) {
        Map<Role, Integer> map = this.roles.get(uuid);
        if (map == null) {
            return false;
        }
        return map.containsKey(role);
    }

    public void setPlayerPermission(UUID uuid, String permission, Permission.Value value) {
        PermissionMap pm = playerPermission.get(uuid);
        if (pm == null) {
            pm = new PermissionMap(new CompoundTag());
            playerPermission.put(uuid, pm);
        }
        pm.setPermission(permission, value);
    }

    public PermissionMap getPlayerPermission(UUID uuid) {
        PermissionMap pm = playerPermission.get(uuid);
        if (pm == null) {
            pm = new PermissionMap(new CompoundTag());
        }
        return pm;
    }

    public void resetPlayerPermission(UUID uuid, String permission) {
        PermissionMap pm = playerPermission.get(uuid);
        if (pm != null) {
            pm.resetPermission(permission);
        }
    }

    private HashMap<Role, Integer> sort(HashMap<Role, Integer> hashMap) {
        List<Map.Entry<Role, Integer>> list = new LinkedList<>(hashMap.entrySet());
        list.sort(Map.Entry.comparingByValue());
        HashMap<Role, Integer> temp = new LinkedHashMap<>();
        temp.put(ItsOursMod.INSTANCE.getRoleManager().get("default"), -1);
        for (Map.Entry<Role, Integer> aa : list) {
            temp.put(aa.getKey(), aa.getValue());
        }
        return temp;
    }

}
