package me.drex.itsours.user;

import net.minecraft.util.math.BlockPos;

public interface ClaimSelectingPlayer {

    boolean arePositionsSet();

    void resetSelection();

    BlockPos getSecondPosition();

    void setSecondPosition(BlockPos pos);

    BlockPos getFirstPosition();

    void setFirstPosition(BlockPos pos);

}
