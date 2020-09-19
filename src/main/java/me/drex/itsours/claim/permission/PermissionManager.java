package me.drex.itsours.claim.permission;

import me.drex.itsours.ItsOursMod;
import me.drex.itsours.claim.permission.roles.Role;
import me.drex.itsours.claim.permission.util.PermissionMap;
import net.minecraft.nbt.CompoundTag;

import java.util.*;

public class PermissionManager {

    //Default claim permissions for players without specified permissions
    public PermissionMap settings;
    public HashMap<UUID, PermissionMap> playerPermission = new HashMap<>();
    public HashMap<UUID, HashMap<Role, Integer>> roles = new HashMap<>();

    public PermissionManager(CompoundTag tag) {
        fromNBT(tag);
    }

    private void fromNBT(CompoundTag tag) {
        settings.fromNBT(tag.getCompound("settings"));
        CompoundTag players = tag.getCompound("players");
        CompoundTag playerPermissions = players.getCompound("permissions");
        playerPermissions.getKeys().forEach(uuid -> playerPermission.put(UUID.fromString(uuid), new PermissionMap(playerPermissions.getCompound(uuid))));
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

    public CompoundTag toNBT() {
        //TODO: Implement
        return new CompoundTag();
    }

    public void addRole(UUID uuid, Role role, int weight) {
        HashMap<Role, Integer> roleWeight = this.roles.get(uuid);
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
        HashMap<Role, Integer> sortedRoles = sort(roles.get(uuid));
        sortedRoles.forEach((role, integer) -> rolesByWeight.add(role));
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
        if (playerPermission.get(uuid).isPermissionSet(permission)) {
            value = playerPermission.get(uuid).getPermission(permission);
        }
        return value;
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
