package me.drex.itsours.claim.permission;

import me.drex.itsours.ItsOursMod;
import me.drex.itsours.claim.permission.roles.Role;
import me.drex.itsours.claim.permission.util.PermissionMap;
import net.minecraft.nbt.CompoundTag;

import java.util.*;

public class PermissionManager {

    //Default claim permissions for players without specified permissions
    public PermissionMap settings = new PermissionMap(new CompoundTag());
    public HashMap<UUID, PermissionMap> playerPermission = new HashMap<>();
    public HashMap<UUID, HashMap<Role, Integer>> roles = new HashMap<>();

    public PermissionManager(CompoundTag tag) {
        fromNBT(tag);
    }

    private void fromNBT(CompoundTag tag) {
        if (tag.contains("settings")) settings.fromNBT(tag.getCompound("settings"));
        if (tag.contains("players")) {
            CompoundTag players = tag.getCompound("players");
            players.getKeys().forEach(uuid -> {
                CompoundTag player = players.getCompound(uuid);
                if (player.contains("permission")) playerPermission.put(UUID.fromString(uuid), new PermissionMap(player.getCompound("permission")));
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

    public List<Role> getRolesByWeight(UUID uuid) {
        List<Role> rolesByWeight = new ArrayList<>();
        if (roles.get(uuid) != null) {
            HashMap<Role, Integer> sortedRoles = sort(roles.get(uuid));
            sortedRoles.forEach((role, integer) -> rolesByWeight.add(role));
        }
        return rolesByWeight;
    }

    public boolean hasPermission(UUID uuid, String permission) {
        boolean value = false;
        if (settings.isPermissionSet(permission)) {
            value = settings.getPermission(permission);
        }
        for (Role role : this.getRolesByWeight(uuid)) {
            if (role.permissions().isPermissionSet(permission)) {
                value = role.permissions().getPermission(permission);
            }
        }
        if (this.isPlayerPermissionSet(uuid, permission)) {
            value = playerPermission.get(uuid).getPermission(permission);
        }
        return value;
    }

    public boolean isPlayerPermissionSet(UUID uuid, String permission) {
        return playerPermission.get(uuid) != null && playerPermission.get(uuid).isPermissionSet(permission);
    }

    public void setPlayerPermission(UUID uuid, String permission, boolean value) {
        PermissionMap pm = playerPermission.get(uuid);
        if (pm == null) {
            pm = new PermissionMap(new CompoundTag());
            playerPermission.put(uuid, pm);
        }
        pm.setPermission(permission, value);
    }

    public HashMap<Role, Integer> sort(HashMap<Role, Integer> hashMap) {
        List<Map.Entry<Role, Integer>> list = new LinkedList<>(hashMap.entrySet());
        list.sort(Map.Entry.comparingByValue());
        HashMap<Role, Integer> temp = new LinkedHashMap<>();
        for (Map.Entry<Role, Integer> aa : list) {
            temp.put(aa.getKey(), aa.getValue());
        }
        return temp;
    }

}
