package me.drex.itsours.claim;

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
    public int getDepth() {
        return 0;
    }


}
