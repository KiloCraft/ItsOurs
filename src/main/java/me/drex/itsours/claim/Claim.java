package me.drex.itsours.claim;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

import java.util.UUID;
import java.util.function.Consumer;

public class Claim extends AbstractClaim {

    public Claim(String name, UUID owner, BlockPos pos1, BlockPos pos2, ServerWorld world, BlockPos tppos) {
        super(name, owner, pos1, pos2, world, tppos);
    }

    public Claim(CompoundTag tag) {
        super(tag);
    }

    @Override
    public int getDepth() {
        return 0;
    }

    @Override
    public boolean isSubzone() {
        return false;
    }
}
