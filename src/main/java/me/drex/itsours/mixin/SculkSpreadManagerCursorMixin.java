package me.drex.itsours.mixin;

import me.drex.itsours.claim.AbstractClaim;
import me.drex.itsours.claim.list.ClaimList;
import me.drex.itsours.claim.flags.Flags;
import net.minecraft.world.level.block.SculkSpreader;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelAccessor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

@Mixin(SculkSpreader.ChargeCursor.class)
public abstract class SculkSpreadManagerCursorMixin {

    /**
     * Prevents sculk charge from crossing claim borders.
     * Sculk veins may still generate on the border of claims.
     */
    @Inject(method = "getValidMovementPos", at = @At("TAIL"), cancellable = true)
    private static void itsours$dontSpreadCrossBorder(LevelAccessor access, BlockPos oldPos, RandomSource random, CallbackInfoReturnable<BlockPos> cir) {
        BlockPos returnValue = cir.getReturnValue();
        if (returnValue == null) return;
        if (!(access instanceof ServerLevel world)) return;
        Optional<AbstractClaim> oldClaim = ClaimList.getClaimAt(world, oldPos);
        Optional<AbstractClaim> newClaim = ClaimList.getClaimAt(world, returnValue);
        if (((oldClaim.isPresent() && !oldClaim.get().checkAction(null, Flags.SCULK_CROSSES_BORDERS)) ||
            (newClaim.isPresent() && !newClaim.get().checkAction(null, Flags.SCULK_CROSSES_BORDERS))) && !newClaim.equals(oldClaim)) {
            cir.setReturnValue(null);
        }
    }

}
