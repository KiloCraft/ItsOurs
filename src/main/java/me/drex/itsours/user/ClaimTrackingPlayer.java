package me.drex.itsours.user;

import me.drex.itsours.claim.AbstractClaim;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.LevelChunk;

import java.util.List;

public interface ClaimTrackingPlayer {

    void addChunkBatch(List<LevelChunk> chunkBatch);

    void onChunkUnload(ChunkPos pos);

    boolean isTracked(BlockPos pos);

    void notifyChange(AbstractClaim claim, boolean add);

    void trackClaims();

    void unTrackClaims();

}
