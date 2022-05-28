package me.drex.itsours.claim.permission.rework.roles;

import me.drex.itsours.claim.permission.rework.PermissionManager;
import me.drex.itsours.claim.permission.rework.PermissionRework;
import me.drex.itsours.claim.permission.rework.PermissionStorage;
import me.drex.itsours.claim.permission.rework.Value;
import me.drex.itsours.claim.permission.rework.node.Node;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Supplier;

public class RoleManagerRework {

    public static final RoleManagerRework INSTANCE = new RoleManagerRework();
    public static final String TRUSTED_ID = "trusted";
    public static final String DEFAULT_ID = "default";
    private static final Map<String, Supplier<RoleRework>> DEFAULT_ROLES = new HashMap<>();
    static {
        DEFAULT_ROLES.put(TRUSTED_ID, RoleManagerRework::defaultTrustedRole);
        DEFAULT_ROLES.put(DEFAULT_ID, () -> new RoleRework(DEFAULT_ID, new NbtCompound()));
    }
    private final Map<String, RoleRework> id2Role = new HashMap<>();
    private final List<RoleRework> orderedRoles = new LinkedList<>();

    public void load(NbtList tag) {
        // Load default roles
        for (Map.Entry<String, Supplier<RoleRework>> entry : DEFAULT_ROLES.entrySet()) {
            addRole(entry.getValue().get());
        }
        for (NbtElement nbtElement : tag) {
            if (nbtElement instanceof NbtCompound nbtCompound) {
                for (String id : nbtCompound.getKeys()) {
                    RoleRework role = new RoleRework(id, nbtCompound.getCompound(id));
                    addRole(role);
                }
            } else {
                throw new IllegalArgumentException("Expected nbt compound, but received " + nbtElement);
            }
        }
    }

    public boolean addRole(RoleRework role) {
        id2Role.put(role.getId(), role);
        boolean replaced = orderedRoles.removeIf(role1 -> role1.getId().equals(role.getId()));
        orderedRoles.add(role);
        return replaced;
    }

    public void removeRole(RoleRework role) {
        String id = role.getId();
        if (DEFAULT_ROLES.containsKey(id)) throw new IllegalArgumentException("This role can't be removed");
        id2Role.remove(id);
        orderedRoles.remove(role);
    }

    public MutableText getName(RoleRework role) {
        boolean isDefault = DEFAULT_ROLES.containsKey(role.getId());
        return Text.literal(role.getId()).formatted(isDefault ? Formatting.GOLD : Formatting.GRAY);
    }

    public boolean updateRoleOrder(RoleRework role, int offSet) {
        int index = orderedRoles.indexOf(role);
        int newIndex = index + offSet;
        if (newIndex >= 0 && newIndex < orderedRoles.size()) {
            Collections.swap(orderedRoles, index, newIndex);
            return true;
        }
        return false;
    }

    // TODO: remove role
    public NbtList save() {
        NbtList nbtList = new NbtList();
        for (RoleRework role : orderedRoles) {
            System.out.println(role.getId());
            NbtCompound nbtCompound = new NbtCompound();
            nbtCompound.put(role.getId(), role.save());
            nbtList.add(nbtCompound);
        }
        System.out.println(nbtList);
        return nbtList;
    }

    private static RoleRework defaultTrustedRole() {
        PermissionStorage permissionStorage = PermissionStorage.storage();
        List<Node> nodes = PermissionManager.PERMISSION.getNodes();
        for (Node node : nodes) {
            if (!node.equals(PermissionManager.MODIFY)) {
                PermissionRework permission = PermissionRework.of(List.of(node));
                permissionStorage.set(permission, Value.ALLOW);
            }
        }
        return new RoleRework(TRUSTED_ID, permissionStorage);
    }

    @Nullable
    public RoleRework getRole(String id) {
        return id2Role.get(id);
    }

    public List<RoleRework> getOrderedRoles() {
        return orderedRoles;
    }

    public Map<String, RoleRework> getRoles() {
        return id2Role;
    }

    @Nullable
    @Deprecated
    public String getRoleId(RoleRework role) {
        for (Map.Entry<String, RoleRework> entry : id2Role.entrySet()) {
            if (entry.getValue() == role) return entry.getKey();
        }
        return null;
    }

}
