package me.drex.itsours.mixin;

import me.drex.itsours.user.ClaimPlayer;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerManager.class)
public class PlayerManagerMixin {

    @Inject(method = "savePlayerData", at = @At(value = "HEAD"))
    public void save(ServerPlayerEntity player, CallbackInfo ci) {
        ClaimPlayer claimPlayer = (ClaimPlayer) player;
//        claimPlayer.save();
    }

}
