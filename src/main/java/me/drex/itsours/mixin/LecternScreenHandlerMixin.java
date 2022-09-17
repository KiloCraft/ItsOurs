package me.drex.itsours.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import me.drex.itsours.claim.AbstractClaim;
import me.drex.itsours.claim.ClaimList;
import me.drex.itsours.claim.permission.PermissionManager;
import me.drex.itsours.claim.permission.node.Node;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.screen.LecternScreenHandler;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.registry.Registry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Optional;

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
        Optional<AbstractClaim> claim = ClaimList.INSTANCE.getClaimAt(player);
        if (claim.isEmpty())
            return original;
        if (!claim.get().hasPermission(player.getUuid(), PermissionManager.MINE, Node.dummy(Registry.BLOCK, Blocks.LECTERN))) {
            player.sendMessage(Text.translatable("text.itsours.action.disallowed.interact_block").formatted(Formatting.RED), true);
            return false;
        }
        return original;
    }


}
