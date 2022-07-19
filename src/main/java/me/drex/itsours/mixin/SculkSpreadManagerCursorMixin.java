package me.drex.itsours.mixin;

import me.drex.itsours.claim.AbstractClaim;
import me.drex.itsours.claim.ClaimList;
import me.drex.itsours.claim.permission.PermissionManager;
import net.minecraft.block.entity.SculkSpreadManager;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.WorldAccess;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

@Mixin(SculkSpreadManager.Cursor.class)
public abstract class SculkSpreadManagerCursorMixin {

    /**
     * Prevents sculk charge from crossing claim borders.
     * Sculk veins may still generate on the border of claims.
    * */
    @Inject(method = "getSpreadPos", at = @At("TAIL"), cancellable = true)
    private static void dontSpreadCrossBorder(WorldAccess world, BlockPos oldPos, Random random, CallbackInfoReturnable<BlockPos> cir) {
        BlockPos returnValue = cir.getReturnValue();
        if (returnValue == null) return;
        // TODO: WorldAccess can't reliable be cast to (ServerWorld)
        Optional<AbstractClaim> oldClaim = ClaimList.INSTANCE.getClaimAt((ServerWorld) world, oldPos);
        Optional<AbstractClaim> newClaim = ClaimList.INSTANCE.getClaimAt((ServerWorld) world, returnValue);
        if (((oldClaim.isPresent() && !oldClaim.get().hasPermission(null, PermissionManager.SCULK_CROSSES_BORDERS)) ||
                (newClaim.isPresent() && !newClaim.get().hasPermission(null, PermissionManager.SCULK_CROSSES_BORDERS))) && !newClaim.equals(oldClaim)) {
            cir.setReturnValue(null);
        }
    }

}
