package me.drex.itsours.claim;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import me.drex.itsours.claim.permission.rework.PermissionInterface;
import me.drex.itsours.claim.permission.rework.PermissionVisitor;
import me.drex.itsours.claim.permission.roles.Role;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class Subzone extends AbstractClaim {

    final AbstractClaim parent;

    public Subzone(String name, UUID owner, BlockPos min, BlockPos max, ServerWorld world, AbstractClaim parent) {
        super(name, owner, min, max, world);
        // Make sure the parent isn't also in the subzone list (getDepth() would get an infinite loop)
        this.parent = parent;
        this.parent.addSubzone(this);
    }

    public Subzone(NbtCompound tag, AbstractClaim parent) {
        super(tag);
        this.parent = parent;
    }

    public AbstractClaim getParent() {
        return this.parent;
    }

    @Override
    public String getFullName() {
        return parent.getFullName() + "." + getName();
    }

    @Override
    public Claim getMainClaim() {
        return getParent().getMainClaim();
    }

    @Override
    public Object2IntMap<Role> getRoles(UUID uuid) {
        Object2IntMap<Role> roles = parent.getRoles(uuid);
        for (Role role : getPermissionManager().getRemovedRoles(uuid)) {
            roles.removeInt(role);
        }
        for (Object2IntMap.Entry<Role> entry : getPermissionManager().getRoles(uuid).object2IntEntrySet()) {
            roles.put(entry.getKey(), entry.getIntValue());
        }
        return roles;
    }

    /*@Override
    public boolean getSetting(String setting) {
        Optional<Permission> optional = Permission.setting(setting);
        if (optional.isPresent()) {
            PermissionContext context = this.getPermissionManager().settings.getPermission(this, optional.get(), Priority.SETTING);
            if (context.getValue() == Permission.Value.UNSET) {
                return parent.getSetting(setting);
            } else {
                return false;
            }
        } else {
            return false;
        }
    }*/

    public int getDepth() {
        return this.parent.getDepth() + 1;
    }

    @Override
    public void visit(@Nullable UUID uuid, PermissionInterface permission, PermissionVisitor visitor) {
        this.parent.visit(uuid, permission, visitor);
        super.visit(uuid, permission, visitor);
    }

}
