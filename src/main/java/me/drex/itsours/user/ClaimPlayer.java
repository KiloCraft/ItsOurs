package me.drex.itsours.user;

import me.drex.itsours.claim.AbstractClaim;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;

public interface ClaimPlayer {

    void setLastShow(AbstractClaim claim, BlockPos pos, ServerWorld world);

    AbstractClaim getLastShowClaim();

    BlockPos getLastShowPos();

    ServerWorld getLastShowWorld();

    boolean arePositionsSet();

    void setSecondPosition(BlockPos pos);

    void setFirstPosition(BlockPos pos);

    BlockPos getSecondPosition();

    BlockPos getFirstPosition();

    void setSelecting(boolean value);

    boolean isSelecting();

    void sendMessage(Text text);

}
