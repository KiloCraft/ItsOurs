package me.drex.itsours.claim;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import me.drex.itsours.ItsOursMod;
import me.drex.itsours.claim.permission.util.Permission;
import net.minecraft.nbt.CompoundTag;
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

    public Subzone(CompoundTag tag, AbstractClaim parent) {
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
    public boolean hasPermission(UUID uuid, String permission) {
        Permission.Value value = this.getPermissionManager().hasPermission(uuid, permission);
        if (value == Permission.Value.UNSET) {
            return parent.hasPermission(uuid, permission);
        }
        sendDebug(uuid, permission, value);
        return value.value;
    }

    @Override
    public boolean getSetting(String setting) {
        Permission.Value value = this.getPermissionManager().settings.getPermission(setting);
        if (value == Permission.Value.UNSET) {
            return parent.getSetting(setting);
        }
        return value.value;
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
        if (this.max.getY() > 256 || this.min.getY() < 0) {
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
