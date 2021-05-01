package me.drex.itsours.user;

import me.drex.itsours.claim.AbstractClaim;
import net.kyori.adventure.text.Component;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;

public interface ClaimPlayer {

    void setLastShow(AbstractClaim claim, BlockPos pos, ServerWorld world);

    AbstractClaim getLastShowClaim();

    BlockPos getLastShowPos();

    ServerWorld getLastShowWorld();

    boolean arePositionsSet();

    void setLeftPosition(BlockPos pos);

    void setRightPosition(BlockPos pos);

    BlockPos getRightPosition();

    BlockPos getLeftPosition();

    void sendError(Component component);

    void sendError(String error);

    void sendMessage(Text text);

    void sendMessage(Component component);

    void sendActionbar(Component component);

    void fromNBT(NbtCompound tag);

    NbtCompound toNBT();

    Object getSetting(String key, Object defaultValue);

    void setSetting(String key, Object value);

}
