package me.drex.itsours.mixin;

import me.drex.itsours.claim.AbstractClaim;
import me.drex.itsours.claim.list.ClaimList;
import me.drex.itsours.claim.flags.Flags;
import me.drex.itsours.claim.flags.node.Node;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.ScaffoldingBlock;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.core.registries.BuiltInRegistries;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

import static me.drex.itsours.util.LocalizedMessage.localized;
import static me.drex.itsours.util.LocalizedMessage.sendMessage;

@Mixin(ScaffoldingBlock.class)
public abstract class ScaffoldingBlockMixin {

    @Inject(method = "getStateForPlacement", at = @At("HEAD"), cancellable = true)
    public void itsours$preventScaffoldingInClaim(BlockPlaceContext ctx, CallbackInfoReturnable<BlockState> cir) {
        if (ctx.getPlayer() == null) return;
        Optional<AbstractClaim> claim = ClaimList.getClaimAt(ctx.getLevel(), ctx.getClickedPos());
        if (claim.isPresent() && !claim.get().checkAction(ctx.getPlayer().getUUID(), Flags.PLACE, Node.registry(BuiltInRegistries.BLOCK, (ScaffoldingBlock) (Object) this))) {
            sendMessage(ctx.getPlayer(), localized("text.itsours.action.disallowed.place_block", ctx.getPlayer()), true);
            cir.setReturnValue(null);
        }
    }

}
