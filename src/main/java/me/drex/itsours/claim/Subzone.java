package me.drex.itsours.claim;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import me.drex.itsours.ItsOursMod;
import me.drex.itsours.claim.permission.Permission;
import me.drex.itsours.claim.permission.roles.Role;
import me.drex.itsours.claim.permission.util.context.PermissionContext;
import me.drex.itsours.claim.permission.util.context.Priority;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.LiteralText;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

import java.util.Optional;
import java.util.UUID;

public class Subzone extends AbstractClaim {

    final AbstractClaim parent;

    public Subzone(String name, UUID owner, BlockPos min, BlockPos max, ServerWorld world, BlockPos tppos, AbstractClaim parent) {
        super(name, owner, min, max, world, tppos);
        //Make sure the parent isnt also in the subzone list (getDepth() would get an infinite loop)
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
    protected PermissionContext getPermissionContext(UUID uuid, Permission permission) {
        PermissionContext context = super.getPermissionContext(uuid, permission);
        context.combine(parent.getPermissionContext(uuid, permission));
        return context;
    }

    @Override
    public Object2IntMap<Role> getRoles(UUID uuid) {
        Object2IntMap<Role> roles = parent.getRoles(uuid);
        for (Role role : getPermissionManager().getRemovedRoles(uuid)) {
            roles.remove(role);
        }
        for (Object2IntMap.Entry<Role> entry : getPermissionManager().getRoles(uuid).object2IntEntrySet()) {
            roles.put(entry.getKey(), entry.getIntValue());
        }
        return roles;
    }

    @Override
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
    }

    public int getDepth() {
        return this.getParent().getDepth() + 1;
    }

    @Override
    public int expand(UUID uuid, Direction direction, int amount) throws CommandSyntaxException {
        int previousArea = this.getArea();
        this.show(false);
        this.expand(direction, amount);
        int requiredBlocks = this.getArea() - previousArea;
        if (!this.isInside()) {
            this.undoExpand(direction, amount);
            throw new SimpleCommandExceptionType(new LiteralText("Expansion would result in " + this.getName() + " being outside of " + this.parent.getName())).create();
        }
        Optional<AbstractClaim> optional = this.intersects();
        if (optional.isPresent()) {
            this.undoExpand(direction, amount);
            throw new SimpleCommandExceptionType(new LiteralText("Expansion would result in hitting " + optional.get())).create();
        }
        if (this.max.getY() > this.getWorld().getTopY() || this.min.getY() < this.getWorld().getBottomY()) {
            this.undoExpand(direction, amount);
            throw new SimpleCommandExceptionType(new LiteralText("You can't expand outside of the world!")).create();
        }
        if (max.getX() < min.getX() || max.getY() < min.getY() || max.getZ() < min.getZ()) {
            this.undoExpand(direction, amount);
            throw new SimpleCommandExceptionType(new LiteralText("You can't shrink your claim that much")).create();
        }
        for (Subzone subzone : this.getSubzones()) {
            if (!subzone.isInside()) {
                this.undoExpand(direction, amount);
                throw new SimpleCommandExceptionType(new LiteralText("Shrinking would result in " + subzone.getName() + " being outside of " + this.getName())).create();
            }
        }
        this.show(true);
        ItsOursMod.INSTANCE.getClaimList().update();
        return requiredBlocks;
    }

    boolean isInside() {
        BlockPos a = min, b = max, c = new BlockPos(max.getX(), min.getY(), min.getZ()), d = new BlockPos(min.getX(), max.getY(), min.getZ()), e = new BlockPos(min.getX(), min.getY(), max.getZ()), f = new BlockPos(max.getX(), max.getY(), min.getZ()), g = new BlockPos(max.getX(), min.getY(), max.getZ()), h = new BlockPos(min.getX(), max.getY(), max.getZ());
        return this.parent.contains(a) && this.parent.contains(b) && this.parent.contains(c) && this.parent.contains(d) && this.parent.contains(e) && this.parent.contains(f) && this.parent.contains(g) && this.parent.contains(h);
    }

}
