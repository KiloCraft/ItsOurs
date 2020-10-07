package me.drex.itsours.mixin;

import me.drex.itsours.ItsOursMod;
import me.drex.itsours.claim.AbstractClaim;
import me.drex.itsours.user.ClaimPlayer;
import net.minecraft.entity.Entity;
import net.minecraft.network.packet.s2c.play.TitleS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Entity.class)
public abstract class EntityMixin {

    private AbstractClaim pclaim = null;

    @Inject(method = "setPos", at = @At("HEAD"))
    public void doPrePosActions(double x, double y, double z, CallbackInfo ci) {
        if ((Object) this instanceof ServerPlayerEntity) {
            ServerPlayerEntity player = (ServerPlayerEntity) (Object) this;
            if (player.getBlockPos() == null) return;
            pclaim = ItsOursMod.INSTANCE.getClaimList().get((ServerWorld) player.world, player.getBlockPos());
        }
    }

    @Inject(method = "setPos", at = @At("RETURN"))
    public void doPostPosActions(double x, double y, double z, CallbackInfo ci) {
        if ((Object) this instanceof ServerPlayerEntity) {
            ServerPlayerEntity player = (ServerPlayerEntity) (Object) this;
            if (player.getBlockPos() == null) return;
            AbstractClaim claim = ItsOursMod.INSTANCE.getClaimList().get((ServerWorld) player.world, player.getBlockPos());
            if (pclaim != claim && player instanceof ServerPlayerEntity) {
                if (player.networkHandler != null) {
                    ClaimPlayer claimPlayer = (ClaimPlayer) player;
                    Text message = null;
                    if (pclaim != null && claim == null) {
                        //TODO: Make configurable
                        player.abilities.allowFlying = claimPlayer.getFlightCache();
                        if (player.abilities.flying && !claimPlayer.getFlightCache()) player.abilities.flying = false;
                        player.sendAbilitiesUpdate();
                        message = new LiteralText("You left " + pclaim.getFullName()).formatted(Formatting.YELLOW);
                    } else if (claim != null) {
                        if (pclaim == null) claimPlayer.cacheFlight(player.abilities.allowFlying);
                        player.abilities.flying = claimPlayer.flightEnabled();
                        player.abilities.allowFlying = claimPlayer.flightEnabled();
                        player.sendAbilitiesUpdate();
                        message = new LiteralText("Welcome to " + claim.getFullName()).formatted(Formatting.YELLOW);
                    }

                    if (message != null) {
                        player.networkHandler.sendPacket(new TitleS2CPacket(TitleS2CPacket.Action.ACTIONBAR, message, -1, 20, -1));
                    }
                }
            }
        }
    }

}
