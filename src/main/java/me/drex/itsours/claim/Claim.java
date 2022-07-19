package me.drex.itsours.claim;

import me.drex.itsours.util.ClaimBox;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;

import java.util.UUID;

public class Claim extends AbstractClaim {

    public Claim(String name, UUID owner, ClaimBox box, ServerWorld world) {
        super(name, owner, box, world);
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
    public int getDepth() {
        return 0;
    }


}
