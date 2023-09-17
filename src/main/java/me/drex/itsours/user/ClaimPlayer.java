package me.drex.itsours.user;

import me.drex.itsours.claim.AbstractClaim;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;

public interface ClaimPlayer {

    void setLastShow(AbstractClaim claim, BlockPos pos, ServerWorld world);

    AbstractClaim getLastShowClaim();

    BlockPos getLastShowPos();

    ServerWorld getLastShowWorld();

    boolean arePositionsSet();

    void resetSelection();

    BlockPos getSecondPosition();

    void setSecondPosition(BlockPos pos);

    BlockPos getFirstPosition();

    void setFirstPosition(BlockPos pos);

}
