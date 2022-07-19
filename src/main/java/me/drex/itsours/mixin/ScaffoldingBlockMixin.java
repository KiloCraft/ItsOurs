package me.drex.itsours.mixin;

import me.drex.itsours.claim.AbstractClaim;
import me.drex.itsours.claim.ClaimList;
import me.drex.itsours.claim.permission.PermissionManager;
import me.drex.itsours.claim.permission.node.Node;
import net.minecraft.block.BlockState;
import net.minecraft.block.ScaffoldingBlock;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.registry.Registry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

@Mixin(ScaffoldingBlock.class)
public abstract class ScaffoldingBlockMixin {

    @Inject(method = "getPlacementState", at = @At("HEAD"), cancellable = true)
    public void preventScaffoldingInClaim(ItemPlacementContext ctx, CallbackInfoReturnable<BlockState> cir) {
        if (ctx.getPlayer() == null) return;
        Optional<AbstractClaim> claim = ClaimList.INSTANCE.getClaimAt((ServerWorld) ctx.getWorld(), ctx.getBlockPos());
        if (claim.isPresent() && !claim.get().hasPermission(ctx.getPlayer().getUuid(), PermissionManager.PLACE, Node.dummy(Registry.BLOCK, (ScaffoldingBlock) (Object) this))) {
            ctx.getPlayer().sendMessage(Text.translatable("text.itsours.action.disallowed.place_block").formatted(Formatting.RED));
            cir.setReturnValue(null);
        }
    }

}
