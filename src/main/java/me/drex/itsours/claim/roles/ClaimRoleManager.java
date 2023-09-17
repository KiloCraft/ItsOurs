package me.drex.itsours.claim.roles;

import com.mojang.serialization.Codec;
import me.drex.itsours.claim.permission.Permission;
import me.drex.itsours.claim.permission.PermissionManager;
import me.drex.itsours.claim.permission.context.RoleContext;
import me.drex.itsours.claim.permission.holder.PermissionData;
import me.drex.itsours.claim.permission.node.ChildNode;
import me.drex.itsours.claim.permission.util.Value;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.registry.entry.RegistryEntryList;
import net.minecraft.registry.tag.ItemTags;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class ClaimRoleManager {

    public static final Codec<ClaimRoleManager> CODEC = Codec.unboundedMap(Codec.STRING, Role.CODEC).xmap(ClaimRoleManager::new, ClaimRoleManager::roles);

    public static final String TRUSTED = "trusted";
    public static final String MODERATOR = "moderator";
    public static final PermissionData DEFAULT_TRUSTED;
    public static final PermissionData DEFAULT_MODERATOR;

    static {
        {
            DEFAULT_TRUSTED = new PermissionData();
            List<ChildNode> nodes = PermissionManager.PERMISSION.getNodes();
            for (ChildNode node : nodes) {
                if (!node.equals(PermissionManager.MODIFY)) {
                    Permission permission = Permission.permission(node);
                    DEFAULT_TRUSTED.set(permission, Value.ALLOW);
                }
            }
        }
        {
            DEFAULT_MODERATOR = new PermissionData();
            List<ChildNode> nodes = PermissionManager.PERMISSION.getNodes();
            for (ChildNode node : nodes) {
                Permission permission = Permission.permission(node);
                DEFAULT_MODERATOR.set(permission, Value.ALLOW);
            }
        }
    }

    private final LinkedHashMap<String, Role> roles;

    public ClaimRoleManager(Map<String, Role> roles) {
        if (roles instanceof LinkedHashMap<String, Role> linkedHashMap) {
            this.roles = linkedHashMap;
        } else {
            // Ensure role order
            this.roles = new LinkedHashMap<>(roles);
        }
    }

    public ClaimRoleManager() {
        this(new LinkedHashMap<>() {{
            put(TRUSTED, new Role(DEFAULT_TRUSTED.copy(), new HashSet<>()));
            put(MODERATOR, new Role(DEFAULT_MODERATOR.copy(), new HashSet<>()));
        }});
    }

    public static Item getRoleIcon(String roleId) {
        Optional<RegistryEntryList.Named<Item>> optional = Registries.ITEM.getEntryList(ItemTags.WOOL);
        if (optional.isPresent()) {
            RegistryEntryList.Named<Item> entries = optional.get();
            return entries.get(Math.abs(roleId.hashCode()) % entries.size()).value();
        } else {
            return Items.STONE;
        }
    }

    public Role createRole(String roleId) {
        return this.roles.put(roleId, new Role());
    }

    public Set<String> getRoleIds() {
        return roles.keySet();
    }

    public boolean removeRole(String roleId) {
        if (roleId.equals(TRUSTED) || roleId.equals(MODERATOR)) {
            return false;
        }
        if (roles.containsKey(roleId)) {
            roles.remove(roleId);
            return true;
        }
        return false;
    }

    @Nullable
    public Role getRole(String roleId) {
        return roles.get(roleId);
    }

    public int getPriority(String roleId) {
        ArrayList<String> roleIds = new ArrayList<>(this.roles.keySet());
        return roleIds.indexOf(roleId);
    }

    public RoleContext createRoleContext(String roleId, Role role) {
        return new RoleContext(roleId, getPriority(roleId), role);
    }

    public Map<String, Role> roles() {
        return this.roles;
    }

}
