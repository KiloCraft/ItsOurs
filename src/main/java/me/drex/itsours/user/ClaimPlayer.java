package me.drex.itsours.user;

import me.drex.itsours.claim.AbstractClaim;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.dimension.DimensionType;

public interface ClaimPlayer {

    void setLastShow(AbstractClaim claim, BlockPos pos, DimensionType dimension);

    AbstractClaim getLastShowClaim();

    BlockPos getLastShowPos();

    DimensionType getLastShowDimension();

    void sendError(Text error);

    void sendError(String error);

    void sendMessage(Text text);

}
