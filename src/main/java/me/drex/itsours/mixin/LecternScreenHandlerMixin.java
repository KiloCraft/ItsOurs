package me.drex.itsours.mixin;

import me.drex.itsours.ItsOursMod;
import me.drex.itsours.claim.AbstractClaim;
import me.drex.itsours.user.ClaimPlayer;
import me.drex.itsours.util.Color;
import net.kyori.adventure.text.Component;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.screen.LecternScreenHandler;
import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Optional;

@Mixin(LecternScreenHandler.class)
public class LecternScreenHandlerMixin {

    @Redirect(method = "onButtonClick", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerEntity;canModifyBlocks()Z"))
    public boolean canTakeBook(PlayerEntity player) {
        Optional<AbstractClaim> claim = ItsOursMod.INSTANCE.getClaimList().get((ServerWorld) player.getEntityWorld(), player.getBlockPos());
        if (!claim.isPresent())
            return player.canModifyBlocks();
        if (!claim.get().hasPermission(player.getUuid(), "mine.lectern")) {
            ClaimPlayer claimPlayer = (ClaimPlayer) player;
            claimPlayer.sendError(Component.text("You can't do that here.").color(Color.RED));
            return false;
        }
        return player.canModifyBlocks();
    }


}
