package me.drex.itsours.claim.permission;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import me.drex.itsours.ItsOurs;
import me.drex.itsours.claim.AbstractClaim;
import me.drex.itsours.claim.permission.rework.Value;
import me.drex.itsours.claim.permission.rework.PermissionInterface;
import me.drex.itsours.claim.permission.rework.PermissionStorage;
import me.drex.itsours.claim.permission.rework.PermissionVisitor;
import me.drex.itsours.claim.permission.rework.context.PersonalContext;
import me.drex.itsours.claim.permission.rework.context.GlobalContext;
import me.drex.itsours.claim.permission.roles.PlayerRoleManager;
import me.drex.itsours.claim.permission.roles.Role;
import net.minecraft.nbt.NbtCompound;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class PermissionManager {
    public PermissionStorage settings = PermissionStorage.storage();
    public HashMap<UUID, PermissionStorage> playerPermission = new HashMap<>();
    public HashMap<UUID, PlayerRoleManager> roleManager = new HashMap<>();

    public PermissionManager(NbtCompound tag) {
        fromNBT(tag);
    }

    private void fromNBT(NbtCompound tag) {
        if (tag.contains("settings")) settings.load(tag.getCompound("settings"));
        if (tag.contains("players")) {
            NbtCompound players = tag.getCompound("players");
            players.getKeys().forEach(uuid -> {
                NbtCompound player = players.getCompound(uuid);
                if (player.contains("permission"))
                    playerPermission.put(UUID.fromString(uuid), PermissionStorage.fromNbt(player.getCompound("permission")));
                if (player.contains("role")) {
                    roleManager.put(UUID.fromString(uuid), new PlayerRoleManager(player.getCompound("role")));
                }
            });
        }
    }

    public NbtCompound toNBT() {
        NbtCompound tag = new NbtCompound();
        NbtCompound players = new NbtCompound();

        List<UUID> uuidSet = new ArrayList<>();
        uuidSet.addAll(playerPermission.keySet());
        uuidSet.addAll(roleManager.keySet());
        for (UUID uuid : uuidSet) {
            boolean shouldSave = false;
            NbtCompound player = new NbtCompound();
            PermissionStorage storage = playerPermission.get(uuid);
            if (storage != null) {
                player.put("permission", storage.save());
                if (storage.size() > 0) shouldSave = true;
            }
            PlayerRoleManager prm = roleManager.get(uuid);
            if (prm != null) {
                player.put("role", prm.toNBT());
                if (prm.getRoles().size() > 0 || prm.getRemoved().size() > 0) shouldSave = true;
            }
            if (shouldSave) players.put(uuid.toString(), player);
        }

        if (!settings.isEmpty()) tag.put("settings", settings.save());
        tag.put("players", players);
        return tag;
    }

    public boolean addRole(UUID uuid, Role role, int weight) {
        PlayerRoleManager playerRoleManager = this.roleManager.getOrDefault(uuid, new PlayerRoleManager(new NbtCompound()));
        boolean changed = playerRoleManager.addRole(role, weight);
        this.roleManager.put(uuid, playerRoleManager);
        return changed;
    }

    public boolean removeRole(UUID uuid, Role role) {
        PlayerRoleManager playerRoleManager = this.roleManager.getOrDefault(uuid, new PlayerRoleManager(new NbtCompound()));
        boolean changed = playerRoleManager.removeRole(role);
        this.roleManager.put(uuid, playerRoleManager);
        return changed;
    }

    public boolean unsetRole(UUID uuid, Role role) {
        PlayerRoleManager playerRoleManager = this.roleManager.getOrDefault(uuid, new PlayerRoleManager(new NbtCompound()));
        boolean changed = playerRoleManager.unsetRole(role);
        this.roleManager.put(uuid, playerRoleManager);
        return changed;
    }

    public Object2IntMap<Role> getRoles(UUID uuid) {
        PlayerRoleManager playerRoleManager = this.roleManager.getOrDefault(uuid, new PlayerRoleManager(new NbtCompound()));
        return playerRoleManager.getRoles();
    }

    public List<Role> getRemovedRoles(UUID uuid) {
        PlayerRoleManager playerRoleManager = this.roleManager.getOrDefault(uuid, new PlayerRoleManager(new NbtCompound()));
        return playerRoleManager.getRemoved();
    }

    public List<UUID> getPlayersWithRole(String role) {
        List<UUID> list = new ArrayList<>();
        Role r = ItsOurs.INSTANCE.getRoleManager().get(role);
        if (r == null) return list;
        for (Map.Entry<UUID, PlayerRoleManager> entry : this.roleManager.entrySet()) {
            if (entry.getValue().getRoles().containsKey(r)) list.add(entry.getKey());
        }
        return list;
    }

    public void visit(AbstractClaim claim, @Nullable UUID uuid, PermissionInterface permission, PermissionVisitor visitor) {
        settings.visit(claim, permission, GlobalContext.INSTANCE, visitor);
        if (uuid != null) {
            PermissionStorage playerPermissionStorage = playerPermission.get(uuid);
            if (playerPermissionStorage != null) {
                playerPermissionStorage.visit(claim, permission, new PersonalContext(uuid), visitor);
                // TODO: roles
            }
        }
    }

    public void setPermission(UUID uuid, PermissionInterface permission, Value value) {
        PermissionStorage storage = playerPermission.get(uuid);
        if (storage == null) {
            storage = PermissionStorage.storage();
            playerPermission.put(uuid, storage);
        }
        storage.set(permission, value);
    }

    public Value getPermission(UUID uuid, PermissionInterface permission) {
        return getPermission(uuid).get(permission);
    }

    public PermissionStorage getPermission(UUID uuid) {
        PermissionStorage storage = playerPermission.get(uuid);
        if (storage == null) return PermissionStorage.storage();
        return storage;
    }

    public PlayerRoleManager getPlayerRoleManager(UUID uuid) {
        PlayerRoleManager prm = roleManager.get(uuid);
        if (prm == null) {
            prm = new PlayerRoleManager(new NbtCompound());
        }
        return prm;
    }

    private HashMap<Role, Integer> sort(HashMap<Role, Integer> hashMap) {
        List<Map.Entry<Role, Integer>> list = new LinkedList<>(hashMap.entrySet());
        list.sort(Map.Entry.comparingByValue());
        HashMap<Role, Integer> temp = new LinkedHashMap<>();
        temp.put(ItsOurs.INSTANCE.getRoleManager().get("default"), -1);
        for (Map.Entry<Role, Integer> entry : list) {
            temp.put(entry.getKey(), entry.getValue());
        }
        return temp;
    }

}
