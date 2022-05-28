package me.drex.itsours.claim.permission.rework.roles;

import me.drex.itsours.claim.permission.rework.PermissionStorage;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class RoleRework implements Comparable<RoleRework> {

    private final String id;
    private final PermissionStorage permissions;

    public RoleRework(String id, PermissionStorage permissions) {
        this.id = id;
        this.permissions = permissions;
    }

    public RoleRework(String id, NbtCompound tag) {
        this(id, PermissionStorage.storage());
        this.load(tag);
    }

    public void load(NbtCompound tag) {
        permissions.load(tag);
    }

    public String getId() {
        return id;
    }

    public NbtCompound save() {
        return permissions.save();
    }

    public PermissionStorage permissions() {
        return this.permissions;
    }

    @Override
    public int compareTo(@NotNull RoleRework other) {
        List<RoleRework> orderedRoles = RoleManagerRework.INSTANCE.getOrderedRoles();
        // Compare index in role orders (lower is better)
        return Integer.compare(orderedRoles.indexOf(other), orderedRoles.indexOf(this));
    }

}
