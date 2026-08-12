package me.drex.itsours.mixin;

import me.drex.itsours.claim.AbstractClaim;
import me.drex.itsours.claim.list.ClaimList;
import me.drex.itsours.claim.flags.Flags;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Optional;

@Mixin(FlowingFluid.class)
public abstract class FluidMixin {

    @Inject(
        method = "spreadTo",
        at = @At("HEAD"),
        cancellable = true
    )
    private void itsours$canFlowAcrossBorder(LevelAccessor world, BlockPos newPos, BlockState state, Direction direction, FluidState fluidState, CallbackInfo ci) {
        BlockPos oldPos = newPos.relative(direction.getOpposite());
        Optional<AbstractClaim> oldClaim = ClaimList.getClaimAt((Level) world, oldPos);
        Optional<AbstractClaim> newClaim = ClaimList.getClaimAt((Level) world, newPos);
        if (((oldClaim.isPresent() && !oldClaim.get().checkAction(null, Flags.FLUID_CROSSES_BORDERS)) ||
            (newClaim.isPresent() && !newClaim.get().checkAction(null, Flags.FLUID_CROSSES_BORDERS))) && !newClaim.equals(oldClaim)) {
            ci.cancel();
        }
    }

}
