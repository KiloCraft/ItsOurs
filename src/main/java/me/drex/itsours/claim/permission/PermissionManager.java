package me.drex.itsours.claim.permission;

import com.google.common.collect.Maps;
import me.drex.itsours.ItsOursMod;
import me.drex.itsours.claim.permission.Permission.Value;
import me.drex.itsours.claim.permission.roles.Role;
import me.drex.itsours.claim.permission.util.PermissionMap;
import me.drex.itsours.claim.permission.util.context.PermissionContext;
import net.minecraft.nbt.NbtCompound;

import java.util.*;

public class PermissionManager {
    public PermissionMap settings_new = new PermissionMap(new NbtCompound());
    public HashMap<UUID, PermissionMap> playerPermission_new = Maps.newHashMap();
    public HashMap<UUID, HashMap<Role, Integer>> roles = Maps.newHashMap();

    public PermissionManager(NbtCompound tag) {
        fromNBT(tag);
    }

    private void fromNBT(NbtCompound tag) {
        if (tag.contains("settings")) settings_new.fromNBT(tag.getCompound("settings"));
        if (tag.contains("players")) {
            NbtCompound players = tag.getCompound("players");
            players.getKeys().forEach(uuid -> {
                NbtCompound player = players.getCompound(uuid);
                if (player.contains("permission"))
                    playerPermission_new.put(UUID.fromString(uuid), new PermissionMap(player.getCompound("permission")));
                if (player.contains("role")) {
                    NbtCompound roleTag = player.getCompound("role");
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

    public NbtCompound toNBT() {
        NbtCompound tag = new NbtCompound();
        NbtCompound players = new NbtCompound();

        List<UUID> uuidSet = new ArrayList<>();
        uuidSet.addAll(playerPermission_new.keySet());
        uuidSet.addAll(roles.keySet());
        for (UUID uuid : uuidSet) {
            NbtCompound player = new NbtCompound();
            PermissionMap pm = playerPermission_new.get(uuid);
            if (pm != null) {
                player.put("permission", pm.toNBT());
            }
            HashMap<Role, Integer> roleMap = roles.get(uuid);
            if (roleMap != null) {
                NbtCompound roleTag = new NbtCompound();
                roleMap.forEach((role, integer) -> {
                    roleTag.putInt(ItsOursMod.INSTANCE.getRoleManager().getRoleID(role), integer);
                });
                player.put("role", roleTag);
            }
            players.put(uuid.toString(), player);
        }

        if (!settings_new.isEmpty()) tag.put("settings", settings_new.toNBT());
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
        roleWeight.put(role, -1);
        //roleWeight.remove(role);
        this.roles.put(uuid, roleWeight);
    }

    public HashMap<Role, Integer> getRolesByWeight(UUID uuid) {
        HashMap<Role, Integer> sortedRoles;
        roles.computeIfAbsent(uuid, k -> new HashMap<>());
        sortedRoles = sort(roles.get(uuid));
        return sortedRoles;
    }

    public PermissionContext hasPermission_new(UUID uuid, Permission permission) {
        PermissionContext context = new PermissionContext();
        context.combine(settings_new.getPermission(permission, PermissionContext.CustomPriority.SETTING));
        for (Map.Entry<Role, Integer> entry : this.getRolesByWeight(uuid).entrySet()) {
            context.combine(entry.getKey().permissions_new().getPermission(permission, new PermissionContext.RolePriority(ItsOursMod.INSTANCE.getRoleManager().getRoleID(entry.getKey()), entry.getValue())));

        }
        if (playerPermission_new.get(uuid) != null) {
            context.combine(playerPermission_new.get(uuid).getPermission(permission, PermissionContext.CustomPriority.PERMISSION));
        }
        if (permission.nodes() > 1) {
            Permission perm = permission.up(1);
            context.combine(hasPermission_new(uuid, perm));
        }
        return context;
    }

    public boolean hasRole(UUID uuid, Role role) {
        Map<Role, Integer> map = this.roles.get(uuid);
        if (map == null) {
            return false;
        }
        return map.containsKey(role);
    }

    //TODO: Use Permission instead of String
    public void setPlayerPermission(UUID uuid, String permission, Value value) {
        PermissionMap pm = playerPermission_new.get(uuid);
        if (pm == null) {
            pm = new PermissionMap(new NbtCompound());
            playerPermission_new.put(uuid, pm);
        }
        pm.setPermission(permission, value);
    }

    public PermissionMap getPlayerPermission(UUID uuid) {
        PermissionMap pm = playerPermission_new.get(uuid);
        if (pm == null) {
            pm = new PermissionMap(new NbtCompound());
        }
        return pm;
    }

    public void resetPlayerPermission(UUID uuid, String permission) {
        PermissionMap pm = playerPermission_new.get(uuid);
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
            if (aa.getValue() != -1) temp.put(aa.getKey(), aa.getValue());
        }
        return temp;
    }

}
