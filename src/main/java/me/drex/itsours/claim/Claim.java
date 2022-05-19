package me.drex.itsours.claim;

import it.unimi.dsi.fastutil.objects.Object2IntArrayMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import me.drex.itsours.ItsOurs;
import me.drex.itsours.claim.permission.roles.Role;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;

import java.util.UUID;

public class Claim extends AbstractClaim {

    public Claim(String name, UUID owner, BlockPos first, BlockPos second, ServerWorld world) {
        super(name, owner, first, second, world);
    }

    public Claim(NbtCompound tag) {
        super(tag);
    }

    @Override
    public String getFullName() {
        return getName();
    }

    @Override
    public Claim getMainClaim() {
        return this;
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


}
