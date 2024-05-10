package me.drex.itsours.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import me.drex.itsours.claim.AbstractClaim;
import me.drex.itsours.claim.list.ClaimList;
import me.drex.itsours.claim.flags.FlagsManager;
import me.drex.itsours.claim.flags.node.Node;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.Registries;
import net.minecraft.screen.LecternScreenHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Optional;

import static me.drex.message.api.LocalizedMessage.localized;

@Mixin(LecternScreenHandler.class)
public abstract class LecternScreenHandlerMixin {

    @ModifyExpressionValue(
        method = "onButtonClick",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/entity/player/PlayerEntity;canModifyBlocks()Z"
        )
    )
    public boolean itsours$canTakeBook(boolean original, PlayerEntity player) {
        Optional<AbstractClaim> claim = ClaimList.getClaimAt(player);
        if (claim.isEmpty())
            return original;
        if (!claim.get().checkAction(player.getUuid(), FlagsManager.MINE, Node.registry(Registries.BLOCK, Blocks.LECTERN))) {
            player.sendMessage(localized("text.itsours.action.disallowed.interact_block"), true);
            return false;
        }
        return original;
    }


}
