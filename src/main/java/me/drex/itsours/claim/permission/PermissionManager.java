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
            if (players.contains("permissions")) {
                CompoundTag playerPermissions = players.getCompound("permissions");
                playerPermissions.getKeys().forEach(uuid -> playerPermission.put(UUID.fromString(uuid), new PermissionMap(playerPermissions.getCompound(uuid))));

            }
            if (players.contains("roles")) {
                CompoundTag playerRoles = players.getCompound("roles");
                HashMap<Role, Integer> roleWeight = new HashMap<>();
                playerRoles.getKeys().forEach(uuid -> {
                    CompoundTag roleTag = playerRoles.getCompound(uuid);
                    roleTag.getKeys().forEach(roleID -> {
                        int weight = roleTag.getInt(roleID);
                        Role role = ItsOursMod.INSTANCE.getRoleManager().get(roleID);
                        roleWeight.put(role, weight);
                    });
                    roles.put(UUID.fromString(uuid), roleWeight);
                });
            }
        }
    }

    public CompoundTag toNBT() {
        CompoundTag tag = new CompoundTag();
        CompoundTag players = new CompoundTag();
        CompoundTag playerPermissions = new CompoundTag();
        playerPermission.forEach((uuid, permissionMap) -> {
            playerPermissions.put(String.valueOf(uuid), permissionMap.toNBT());
        });
        CompoundTag playerRoles = new CompoundTag();
        roles.forEach((uuid, roleMap) -> {
            CompoundTag roleTag = new CompoundTag();
            roleMap.forEach((role, integer) -> {
                roleTag.putInt(ItsOursMod.INSTANCE.getRoleManager().getRoleID(role), integer);
            });
            playerRoles.put(String.valueOf(uuid), roleTag);
        });


        players.put("permissions", playerPermissions);
        players.put("roles", playerRoles);
        if (settings != null) tag.put("settings", settings.toNBT());
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
