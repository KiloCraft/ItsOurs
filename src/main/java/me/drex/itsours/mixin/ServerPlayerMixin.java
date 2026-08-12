package me.drex.itsours.mixin;

import com.mojang.authlib.GameProfile;
import me.drex.itsours.claim.AbstractClaim;
import me.drex.itsours.data.DataManager;
import me.drex.itsours.user.ClaimSelectingPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(ServerPlayer.class)
public abstract class ServerPlayerMixin extends Player implements ClaimSelectingPlayer {

    @Nullable
    private BlockPos firstPos = null;
    @Nullable
    private BlockPos secondPos = null;

    private AbstractClaim claim = null;

    public ServerPlayerMixin(Level world, GameProfile profile) {
        super(world, profile);
    }

    @Shadow
    public abstract void sendSystemMessage(Component message);

    @Override
    public boolean arePositionsSet() {
        return firstPos != null && secondPos != null;
    }

    @Override
    public void resetSelection() {
        this.firstPos = null;
        this.secondPos = null;
        DataManager.updateUserData(getUUID()).setSelect(false);
    }

    @Override
    public BlockPos getSecondPosition() {
        return secondPos;
    }

    @Override
    public void setSecondPosition(BlockPos pos) {
        secondPos = pos;
    }

    @Override
    public BlockPos getFirstPosition() {
        return firstPos;
    }

    @Override
    public void setFirstPosition(BlockPos pos) {
        firstPos = pos;
    }

    @Override
    public AbstractClaim claim() {
        return claim;
    }

    @Override
    public void setClaim(AbstractClaim claim) {
        this.claim = claim;
    }

}
