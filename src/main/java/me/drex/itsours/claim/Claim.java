package me.drex.itsours.claim;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import it.unimi.dsi.fastutil.objects.Object2IntArrayMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import me.drex.itsours.ItsOurs;
import me.drex.itsours.claim.permission.roles.Role;
import me.drex.itsours.user.PlayerList;
import me.drex.itsours.user.Settings;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
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
        Role def = ItsOurs.INSTANCE.getRoleManager().getRole("default");
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
            throw new SimpleCommandExceptionType(Text.translatable("text.itsours.commands.exception.expand.missing_blocks")).create();
        }
        Optional<AbstractClaim> optional = this.intersects();
        if (optional.isPresent()) {
            this.undoExpand(direction, amount);
            throw new SimpleCommandExceptionType(Text.translatable("text.itsours.commands.exception.intersects", optional.get().getFullName())).create();
        }
        if (this.max.getY() > this.getWorld().getTopY() || this.min.getY() < this.getWorld().getBottomY()) {
            this.undoExpand(direction, amount);
            throw new SimpleCommandExceptionType(Text.translatable("text.itsours.commands.exception.outside_of_world")).create();
        }
        if (max.getX() < min.getX() || max.getY() < min.getY() || max.getZ() < min.getZ()) {
            this.undoExpand(direction, amount);
            throw new SimpleCommandExceptionType(Text.translatable("text.itsours.commands.exception.cant_shrink")).create();
        }
        for (Subzone subzone : this.getSubzones()) {
            if (!subzone.isInside()) {
                this.undoExpand(direction, amount);
                throw new SimpleCommandExceptionType(Text.translatable("text.itsours.commands.exception.subzone_outside", subzone.getFullName(), this.getFullName())).create();
            }
        }
        this.show(true);
        ClaimList.INSTANCE.removeClaim(this);
        ClaimList.INSTANCE.addClaim(this);
        return requiredBlocks;
    }


}
