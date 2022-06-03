package me.drex.itsours.mixin;

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
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Optional;

@Mixin(LecternScreenHandler.class)
public abstract class LecternScreenHandlerMixin {

    @Redirect(
            method = "onButtonClick",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/entity/player/PlayerEntity;canModifyBlocks()Z"
            )
    )
    public boolean canTakeBook(PlayerEntity player) {
        Optional<AbstractClaim> claim = ClaimList.INSTANCE.getClaimAt(player);
        if (claim.isEmpty())
            return player.canModifyBlocks();
        if (!claim.get().hasPermission(player.getUuid(), PermissionManager.MINE, Node.dummy(Registry.BLOCK, Blocks.LECTERN))) {
            player.sendMessage(Text.translatable("text.itsours.action.disallowed.interact_block").formatted(Formatting.RED));
            return false;
        }
        return player.canModifyBlocks();
    }


}
