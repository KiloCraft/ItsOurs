package me.drex.itsours.mixin;

import com.mojang.authlib.GameProfile;
import me.drex.itsours.claim.AbstractClaim;
import me.drex.itsours.data.DataManager;
import me.drex.itsours.user.ClaimSelectingPlayer;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerEntityMixin extends PlayerEntity implements ClaimSelectingPlayer {

    @Nullable
    private BlockPos firstPos = null;
    @Nullable
    private BlockPos secondPos = null;

    private AbstractClaim claim = null;


    public ServerPlayerEntityMixin(World world, BlockPos pos, float yaw, GameProfile profile) {
        super(world, pos, yaw, profile);
    }

    @Shadow
    public abstract void sendMessage(Text message);

    @Override
    public boolean arePositionsSet() {
        return firstPos != null && secondPos != null;
    }

    @Override
    public void resetSelection() {
        this.firstPos = null;
        this.secondPos = null;
        DataManager.updateUserData(getUuid()).setSelect(false);
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
