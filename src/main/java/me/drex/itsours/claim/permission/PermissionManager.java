package me.drex.itsours.claim.permission;

import me.drex.itsours.ItsOursMod;
import me.drex.itsours.claim.permission.roles.Role;
import me.drex.itsours.claim.permission.util.PermissionMap;
import net.minecraft.nbt.CompoundTag;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PermissionManager {

    //Default claim permissions for players without specified permissions
    public PermissionMap settings;
    public Map<UUID, PermissionMap> playerPermission = new HashMap<>();
    public Map<UUID, Map<Role, Integer>> roles = new HashMap<>();

    public PermissionManager(CompoundTag tag) {
        fromNBT(tag);
    }

    private void fromNBT(CompoundTag tag) {
        settings.fromNBT(tag.getCompound("settings"));
        CompoundTag players = tag.getCompound("players");
        CompoundTag playerPermissions = players.getCompound("permissions");
        playerPermissions.getKeys().forEach(uuid -> playerPermission.put(UUID.fromString(uuid), new PermissionMap(playerPermissions.getCompound(uuid))));
        CompoundTag playerRoles = players.getCompound("roles");
        Map<Role, Integer> roleWeight = new HashMap<>();
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
        Map<Role, Integer> roleWeight = this.roles.get(uuid);
        roleWeight.put(role, weight);
        this.roles.put(uuid, roleWeight);
    }

    public void removeRole(UUID uuid, Role role) {
        Map<Role, Integer> roleWeight = this.roles.get(uuid);
        roleWeight.remove(role);
        this.roles.put(uuid, roleWeight);
    }

}
