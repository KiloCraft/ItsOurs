package me.drex.itsours.mixin;

import me.drex.itsours.claim.AbstractClaim;
import me.drex.itsours.claim.list.ClaimList;
import me.drex.itsours.claim.flags.FlagsManager;
import me.drex.itsours.claim.flags.node.Node;
import net.minecraft.block.BlockState;
import net.minecraft.block.ScaffoldingBlock;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.registry.Registries;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

import static me.drex.message.api.LocalizedMessage.localized;

@Mixin(ScaffoldingBlock.class)
public abstract class ScaffoldingBlockMixin {

    @Inject(method = "getPlacementState", at = @At("HEAD"), cancellable = true)
    public void itsours$preventScaffoldingInClaim(ItemPlacementContext ctx, CallbackInfoReturnable<BlockState> cir) {
        if (ctx.getPlayer() == null) return;
        Optional<AbstractClaim> claim = ClaimList.getClaimAt(ctx.getWorld(), ctx.getBlockPos());
        if (claim.isPresent() && !claim.get().checkAction(ctx.getPlayer().getUuid(), FlagsManager.PLACE, Node.registry(Registries.BLOCK, (ScaffoldingBlock) (Object) this))) {
            ctx.getPlayer().sendMessage(localized("text.itsours.action.disallowed.place_block"), true);
            cir.setReturnValue(null);
        }
    }

}
