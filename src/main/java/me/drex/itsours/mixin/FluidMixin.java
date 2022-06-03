package me.drex.itsours.mixin;

import me.drex.itsours.claim.AbstractClaim;
import me.drex.itsours.claim.ClaimList;
import me.drex.itsours.claim.permission.PermissionManager;
import net.minecraft.block.BlockState;
import net.minecraft.fluid.FlowableFluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.WorldAccess;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Optional;

@Mixin(FlowableFluid.class)
public abstract class FluidMixin {

    @Inject(
            method = "flow",
            at = @At("HEAD"),
            cancellable = true
    )
    private void canFlowAcrossBorder(WorldAccess world, BlockPos newPos, BlockState state, Direction direction, FluidState fluidState, CallbackInfo ci) {
        BlockPos oldPos = newPos.offset(direction.getOpposite());
        Optional<AbstractClaim> oldClaim = ClaimList.INSTANCE.getClaimAt((ServerWorld) world, oldPos);
        Optional<AbstractClaim> newClaim = ClaimList.INSTANCE.getClaimAt((ServerWorld) world, newPos);
        if (((oldClaim.isPresent() && !oldClaim.get().hasPermission(null, PermissionManager.FLUID_CROSSES_BORDERS)) ||
                (newClaim.isPresent() && !newClaim.get().hasPermission(null, PermissionManager.FLUID_CROSSES_BORDERS))) && !newClaim.equals(oldClaim)) {
            ci.cancel();
        }
    }

}
