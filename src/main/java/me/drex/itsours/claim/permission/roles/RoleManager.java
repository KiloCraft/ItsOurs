package me.drex.itsours.claim.permission.roles;

import me.drex.itsours.ItsOurs;
import me.drex.itsours.claim.permission.PermissionImpl;
import me.drex.itsours.claim.permission.PermissionManager;
import me.drex.itsours.claim.permission.holder.PermissionHolder;
import me.drex.itsours.claim.permission.node.Node;
import me.drex.itsours.claim.permission.util.Value;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Supplier;

public class RoleManager {

    public static final RoleManager INSTANCE = new RoleManager();
    public static final String TRUSTED_ID = "trusted";
    public static final String DEFAULT_ID = "default";
    private static final Map<String, Supplier<Role>> DEFAULT_ROLES = new HashMap<>();

    static {
        DEFAULT_ROLES.put(TRUSTED_ID, RoleManager::defaultTrustedRole);
        DEFAULT_ROLES.put(DEFAULT_ID, () -> new Role(DEFAULT_ID, new NbtCompound()));
    }

    private final Map<String, Role> id2Role = new HashMap<>();
    private final List<Role> orderedRoles = new LinkedList<>();

    public void load(NbtElement element) {
        // Load default roles
        for (Map.Entry<String, Supplier<Role>> entry : DEFAULT_ROLES.entrySet()) {
            addRole(entry.getValue().get());
        }
        if (ItsOurs.INSTANCE.getDataVersion() > 1) {
            if (!(element instanceof NbtList list))
                throw new IllegalArgumentException("Expected nbt compound, but received " + element);

            for (NbtElement nbtElement : list) {
                if (!(nbtElement instanceof NbtCompound nbtCompound))
                    throw new IllegalArgumentException("Expected nbt compound, but received " + nbtElement);

                for (String id : nbtCompound.getKeys()) {
                    Role role = new Role(id, nbtCompound.getCompound(id));
                    addRole(role);
                }
            }
        } else {
            if (!(element instanceof NbtCompound nbtCompound))
                throw new IllegalArgumentException("Expected nbt compound, but received " + element);
            for (String id : nbtCompound.getKeys()) {
                Role role = new Role(id, nbtCompound.getCompound(id));
                addRole(role);
            }
        }
    }

    public boolean addRole(Role role) {
        id2Role.put(role.getId(), role);
        boolean replaced = orderedRoles.removeIf(role1 -> role1.getId().equals(role.getId()));
        orderedRoles.add(role);
        return replaced;
    }

    public void removeRole(Role role) {
        String id = role.getId();
        if (DEFAULT_ROLES.containsKey(id)) throw new IllegalArgumentException("This role can't be removed");
        id2Role.remove(id);
        orderedRoles.remove(role);
    }

    public MutableText getName(Role role) {
        boolean isDefault = DEFAULT_ROLES.containsKey(role.getId());
        return Text.literal(role.getId()).formatted(isDefault ? Formatting.GOLD : Formatting.GRAY);
    }

    public boolean updateRoleOrder(Role role, int offSet) {
        int index = orderedRoles.indexOf(role);
        int newIndex = index + offSet;
        if (newIndex >= 0 && newIndex < orderedRoles.size()) {
            Collections.swap(orderedRoles, index, newIndex);
            return true;
        }
        return false;
    }

    public NbtList save() {
        NbtList nbtList = new NbtList();
        for (Role role : orderedRoles) {
            NbtCompound nbtCompound = new NbtCompound();
            nbtCompound.put(role.getId(), role.save());
            nbtList.add(nbtCompound);
        }
        return nbtList;
    }

    private static Role defaultTrustedRole() {
        PermissionHolder permissionStorage = PermissionHolder.storage();
        List<Node> nodes = PermissionManager.PERMISSION.getNodes();
        for (Node node : nodes) {
            if (!node.equals(PermissionManager.MODIFY)) {
                PermissionImpl permission = PermissionImpl.withNodes(node);
                permissionStorage.set(permission, Value.ALLOW);
            }
        }
        return new Role(TRUSTED_ID, permissionStorage);
    }

    @Nullable
    public Role getRole(String id) {
        return id2Role.get(id);
    }

    public List<Role> getOrderedRoles() {
        return orderedRoles;
    }

    public Map<String, Role> getRoles() {
        return id2Role;
    }

    @Nullable
    @Deprecated
    public String getRoleId(Role role) {
        for (Map.Entry<String, Role> entry : id2Role.entrySet()) {
            if (entry.getValue() == role) return entry.getKey();
        }
        return null;
    }

}
