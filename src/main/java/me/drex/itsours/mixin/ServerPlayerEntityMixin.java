package me.drex.itsours.mixin;

import com.mojang.authlib.GameProfile;
import me.drex.itsours.claim.AbstractClaim;
import me.drex.itsours.user.ClaimPlayer;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.network.ServerPlayerInteractionManager;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.GameMode;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayerEntity.class)
public class ServerPlayerEntityMixin extends PlayerEntity implements ClaimPlayer {

    @Shadow @Final public ServerPlayerInteractionManager interactionManager;
    private AbstractClaim lastShowClaim;
    private BlockPos lastShowPos;
    private DimensionType lastShowDimension;
    private int cooldown = 0;

    public ServerPlayerEntityMixin(World world, BlockPos pos, float yaw, GameProfile profile) {
        super(world, pos, yaw, profile);
    }

    public boolean isSpectator() {
        return this.interactionManager.getGameMode() == GameMode.SPECTATOR;
    }

    public boolean isCreative() {
        return this.interactionManager.getGameMode() == GameMode.CREATIVE;
    }


    @Override
    public void setLastShow(AbstractClaim claim, BlockPos pos, DimensionType dimension) {
        this.lastShowClaim = claim;
        this.lastShowPos = pos;
        this.lastShowDimension = dimension;
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
    public DimensionType getLastShowDimension() {
        return this.lastShowDimension;
    }

    @Override
    public void sendError(String error) {
        if (cooldown == 0) {
            this.sendMessage(new LiteralText(error), false);
            //TODO: Make configurable
            cooldown = 20;
        }
    }

    @Override
    public void sendError(Text error) {
        if (cooldown == 0) {
            this.sendMessage(error, false);
            //TODO: Make configurable
            cooldown = 20;
        }
    }

    @Override
    public void sendMessage(Text message) {
        this.sendMessage(message, false);
    }

    @Inject(method = "tick", at = @At(value = "HEAD"))
    private void claimPlayer$onTick(CallbackInfo ci) {
        if (cooldown > 0) cooldown--;
    }
}
