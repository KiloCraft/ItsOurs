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
import me.drex.itsours.claim.permission.rework.context.RoleContext;
import me.drex.itsours.claim.permission.rework.roles.PlayerRoleStorage;
import me.drex.itsours.claim.permission.rework.roles.RoleManagerRework;
import me.drex.itsours.claim.permission.rework.roles.RoleRework;
import me.drex.itsours.claim.permission.roles.PlayerRoleManager;
import me.drex.itsours.claim.permission.roles.Role;
import net.minecraft.nbt.NbtCompound;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class PermissionManager {

    public PermissionStorage settings = PermissionStorage.storage();
    public Map<UUID, PermissionStorage> playerPermission = new HashMap<>();
    public Map<UUID, PlayerRoleStorage> roles = new HashMap<>();

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
                    roles.put(UUID.fromString(uuid), new PlayerRoleStorage(player.get("role")));
                }
            });
        }
    }

    public NbtCompound toNBT() {
        NbtCompound tag = new NbtCompound();
        NbtCompound players = new NbtCompound();

        List<UUID> uuidSet = new ArrayList<>();
        uuidSet.addAll(playerPermission.keySet());
        uuidSet.addAll(roles.keySet());
        for (UUID uuid : uuidSet) {
            boolean shouldSave = false;
            NbtCompound player = new NbtCompound();
            PermissionStorage storage = playerPermission.get(uuid);
            if (storage != null) {
                player.put("permission", storage.save());
                if (storage.size() > 0) shouldSave = true;
            }
            PlayerRoleStorage roleStorage = roles.get(uuid);
            if (roleStorage != null) {
                player.put("role", roleStorage.toNbt());
                if (roleStorage.getSize() > 0) shouldSave = true;
            }
            if (shouldSave) players.put(uuid.toString(), player);
        }

        if (!settings.isEmpty()) tag.put("settings", settings.save());
        tag.put("players", players);
        return tag;
    }

    public boolean addRole(UUID uuid, RoleRework role) {
        PlayerRoleStorage roleStorage = this.roles.computeIfAbsent(uuid, ignored -> new PlayerRoleStorage());
        return roleStorage.addRole(role);
    }

    public boolean removeRole(UUID uuid, RoleRework role) {
        PlayerRoleStorage roleStorage = this.roles.computeIfAbsent(uuid, ignored -> new PlayerRoleStorage());
        return roleStorage.removeRole(role);
    }

    public List<RoleRework> getRolesNew(UUID uuid) {
        PlayerRoleStorage roleStorage = this.roles.get(uuid);
        if (roleStorage != null) {
            return roleStorage.getRoles();
        }
        return Collections.emptyList();
    }

    public Map<UUID, PlayerRoleStorage> getRoles() {
        return roles;
    }

    @Deprecated
    public boolean addRole(UUID uuid, Role role, int weight) {
        throw new UnsupportedOperationException();
        /*PlayerRoleManager playerRoleManager = this.roleManager.getOrDefault(uuid, new PlayerRoleManager(new NbtCompound()));
        boolean changed = playerRoleManager.addRole(role, weight);
        this.roleManager.put(uuid, playerRoleManager);
        return changed;*/
    }

    public boolean removeRole(UUID uuid, Role role) {
        throw new UnsupportedOperationException();
        /*PlayerRoleManager playerRoleManager = this.roleManager.getOrDefault(uuid, new PlayerRoleManager(new NbtCompound()));
        boolean changed = playerRoleManager.removeRole(role);
        this.roleManager.put(uuid, playerRoleManager);
        return changed;*/
    }

    @Deprecated
    public boolean unsetRole(UUID uuid, Role role) {
        throw new UnsupportedOperationException();
        /*PlayerRoleManager playerRoleManager = this.roleManager.getOrDefault(uuid, new PlayerRoleManager(new NbtCompound()));
        boolean changed = playerRoleManager.unsetRole(role);
        this.roleManager.put(uuid, playerRoleManager);
        return changed;*/
    }

    @Deprecated
    public Object2IntMap<Role> getRoles(UUID uuid) {
        throw new UnsupportedOperationException();
        /*PlayerRoleManager playerRoleManager = this.roleManager.getOrDefault(uuid, new PlayerRoleManager(new NbtCompound()));
        return playerRoleManager.getRoles();*/
    }

    @Deprecated
    public List<Role> getRemovedRoles(UUID uuid) {
        throw new UnsupportedOperationException();
        /*PlayerRoleManager playerRoleManager = this.roleManager.getOrDefault(uuid, new PlayerRoleManager(new NbtCompound()));
        return playerRoleManager.getRemoved();*/
    }

    public void visit(AbstractClaim claim, @Nullable UUID uuid, PermissionInterface permission, PermissionVisitor visitor) {
        settings.visit(claim, permission, GlobalContext.INSTANCE, visitor);
        if (uuid != null) {
            PermissionStorage permissionStorage = playerPermission.get(uuid);
            if (permissionStorage != null) {
                permissionStorage.visit(claim, permission, new PersonalContext(uuid), visitor);
                // TODO: roles
            }
            PlayerRoleStorage roleStorage = roles.get(uuid);
            if (roleStorage != null) {
                RoleRework defaultRole = RoleManagerRework.INSTANCE.getRole(RoleManagerRework.DEFAULT_ID);
                assert defaultRole != null;
                defaultRole.permissions().visit(claim, permission, new RoleContext(defaultRole), visitor);
                for (RoleRework role : roleStorage.getRoles()) {
                    role.permissions().visit(claim, permission, new RoleContext(role), visitor);
                }
            }
        }
    }

    public void setPermission(UUID uuid, PermissionInterface permission, Value value) {
        PermissionStorage storage = playerPermission.computeIfAbsent(uuid, ignored -> PermissionStorage.storage());
        storage.set(permission, value);
    }

    public Value getPermission(UUID uuid, PermissionInterface permission) {
        return getPermission(uuid).get(permission);
    }

    public PermissionStorage getPermission(UUID uuid) {
        return playerPermission.computeIfAbsent(uuid, ignored -> PermissionStorage.storage());
    }

    public PlayerRoleManager getPlayerRoleManager(UUID uuid) {
        throw new UnsupportedOperationException();
        /*PlayerRoleManager prm = roleManager.get(uuid);
        if (prm == null) {
            prm = new PlayerRoleManager(new NbtCompound());
        }
        return prm;*/
    }

    @Deprecated
    private HashMap<Role, Integer> sort(HashMap<Role, Integer> hashMap) {
        throw new UnsupportedOperationException();
        /*List<Map.Entry<Role, Integer>> list = new LinkedList<>(hashMap.entrySet());
        list.sort(Map.Entry.comparingByValue());
        HashMap<Role, Integer> temp = new LinkedHashMap<>();
        temp.put(ItsOurs.INSTANCE.getRoleManager().get("default"), -1);
        for (Map.Entry<Role, Integer> entry : list) {
            temp.put(entry.getKey(), entry.getValue());
        }
        return temp;*/
    }

}
