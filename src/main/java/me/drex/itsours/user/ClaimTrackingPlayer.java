package me.drex.itsours.user;

import me.drex.itsours.claim.Claim;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;

public interface ClaimTrackingPlayer {

    void onChunkLoad(ChunkPos pos);

    void onChunkUnload(ChunkPos pos);

    Claim trackedClaim();

    boolean isTracked(BlockPos pos);

    void trackClaim(Claim claim);

    void unTrackClaim();

}
