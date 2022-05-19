package me.drex.itsours.mixin;

import com.mojang.authlib.GameProfile;
import me.drex.itsours.claim.AbstractClaim;
import me.drex.itsours.user.ClaimPlayer;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.encryption.PlayerPublicKey;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerEntityMixin extends PlayerEntity implements ClaimPlayer {

    @Shadow public abstract void sendMessage(Text message);

    public ServerPlayerEntityMixin(World world, BlockPos pos, float yaw, GameProfile profile, @Nullable PlayerPublicKey playerPublicKey) {
        super(world, pos, yaw, profile, playerPublicKey);
    }

    @Nullable
    private BlockPos firstPos = null;
    @Nullable
    private BlockPos secondPos = null;
    private AbstractClaim lastShowClaim;
    private BlockPos lastShowPos;
    private ServerWorld lastShowWorld;
    private boolean select = false;

    @Override
    public void setLastShow(AbstractClaim claim, BlockPos pos, ServerWorld world) {
        this.lastShowClaim = claim;
        this.lastShowPos = pos;
        this.lastShowWorld = world;
    }

    @Override
    public AbstractClaim getLastShowClaim() {
        return this.lastShowClaim;
    }

    @Override
    public BlockPos getLastShowPos() {
        return this.lastShowPos;
    }

    @Override
    public ServerWorld getLastShowWorld() {
        return this.lastShowWorld;
    }

    @Override
    public boolean arePositionsSet() {
        return firstPos != null && secondPos != null;
    }

    @Override
    public BlockPos getSecondPosition() {
        return secondPos;
    }

    @Override
    public void setFirstPosition(BlockPos pos) {
        firstPos = pos;
    }

    @Override
    public BlockPos getFirstPosition() {
        return firstPos;
    }

    @Override
    public void setSecondPosition(BlockPos pos) {
        secondPos = pos;
    }

    @Override
    public boolean isSelecting() {
        return this.select;
    }

    @Override
    public void setSelecting(boolean value) {
        this.select = value;
    }

    @Override
    public void sendText(Text message) {
        this.sendMessage(message);
    }

}
