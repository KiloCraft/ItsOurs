package me.drex.itsours.user;

import me.drex.itsours.claim.AbstractClaim;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.chunk.WorldChunk;

import java.util.List;

public interface ClaimTrackingPlayer {

    void addChunkBatch(List<WorldChunk> chunkBatch);

    void onChunkUnload(ChunkPos pos);

    boolean isTracked(BlockPos pos);

    void notifyChange(AbstractClaim claim, boolean add);

    void trackClaims();

    void unTrackClaims();

}
