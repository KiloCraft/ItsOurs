package me.drex.itsours.user;

import me.drex.itsours.claim.AbstractClaim;
import net.minecraft.core.BlockPos;

public interface ClaimSelectingPlayer {

    boolean arePositionsSet();

    void resetSelection();

    BlockPos getSecondPosition();

    void setSecondPosition(BlockPos pos);

    BlockPos getFirstPosition();

    void setFirstPosition(BlockPos pos);

    AbstractClaim claim();

    void setClaim(AbstractClaim claim);
}
