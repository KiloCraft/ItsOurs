package me.drex.itsours.user;

import me.drex.itsours.claim.AbstractClaim;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.dimension.DimensionType;

public interface ClaimPlayer {

    public void setClaimBlocks(int amount);

    public void addClaimBlocks(int amount);

    public void removeClaimBlocks(int amount);

    public int getClaimBlocks();

    public void setLastShow(AbstractClaim claim, BlockPos pos, DimensionType dimension);

    public AbstractClaim getLastShowClaim();

    public BlockPos getLastShowPos();

    public DimensionType getLastShowDimension();

    public void sendError(Text error);

    public void sendError(String error);

    public void sendMessage(Text text);

}
