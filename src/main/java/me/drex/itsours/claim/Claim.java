package me.drex.itsours.claim;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import it.unimi.dsi.fastutil.objects.Object2IntArrayMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import me.drex.itsours.ItsOursMod;
import me.drex.itsours.claim.permission.roles.Role;
import me.drex.itsours.user.PlayerList;
import me.drex.itsours.user.Settings;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.LiteralText;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

import java.util.Optional;
import java.util.UUID;

public class Claim extends AbstractClaim {

    public Claim(String name, UUID owner, BlockPos pos1, BlockPos pos2, ServerWorld world, BlockPos tppos) {
        super(name, owner, pos1, pos2, world, tppos);
    }

    public Claim(NbtCompound tag) {
        super(tag);
    }

    @Override
    public String getFullName() {
        return getName();
    }

    @Override
    public Object2IntMap<Role> getRoles(UUID uuid) {
        Object2IntMap<Role> roles = getPermissionManager().getRoles(uuid);
        Role def = ItsOursMod.INSTANCE.getRoleManager().getRole("default");
        if (!getPermissionManager().getRemovedRoles(uuid).contains(def)) {
            final Object2IntMap<Role> copy = new Object2IntArrayMap<>();
            copy.putAll(roles);
            copy.put(def, -1);
            return copy;
        }
        return roles;
    }

    @Override
    public int getDepth() {
        return 0;
    }

    @Override
    public int expand(UUID uuid, Direction direction, int amount) throws CommandSyntaxException {
        int previousArea = this.getArea();
        this.show(false);
        this.expand(direction, amount);
        int requiredBlocks = this.getArea() - previousArea;
        if (PlayerList.get(uuid, Settings.BLOCKS) < requiredBlocks) {
            this.undoExpand(direction, amount);
            throw new SimpleCommandExceptionType(new LiteralText("You don't have enough claim blocks!")).create();
        }
        Optional<AbstractClaim> optional = this.intersects();
        if (optional.isPresent()) {
            this.undoExpand(direction, amount);
            throw new SimpleCommandExceptionType(new LiteralText("You can't expand into " + optional.get().getName())).create();
        }
        if (this.max.getY() > this.getWorld().getTopY() || this.min.getY() < this.getWorld().getBottomY()) {
            this.undoExpand(direction, amount);
            throw new SimpleCommandExceptionType(new LiteralText("You can't expand outside of the world!")).create();
        }
        if (max.getX() - min.getX() > 1024 || max.getZ() - min.getZ() > 1024) {
            this.undoExpand(direction, amount);
            throw new SimpleCommandExceptionType(new LiteralText("You can't expand further than 1024 blocks")).create();
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


}
