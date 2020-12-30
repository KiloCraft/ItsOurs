package me.drex.itsours.mixin;

import me.drex.itsours.ItsOursMod;
import me.drex.itsours.claim.AbstractClaim;
import me.drex.itsours.claim.Claim;
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
public class FluidMixin {

    @Inject(method = "flow", at = @At("HEAD"), cancellable = true)
    private void dontFlow(WorldAccess world, BlockPos newPos, BlockState state, Direction direction, FluidState fluidState, CallbackInfo ci) {
        BlockPos oldPos = newPos.offset(direction.getOpposite());
        Optional<AbstractClaim> oldClaim = ItsOursMod.INSTANCE.getClaimList().get((ServerWorld) world, oldPos);
        Optional<AbstractClaim> newClaim = ItsOursMod.INSTANCE.getClaimList().get((ServerWorld) world, newPos);
        if (((oldClaim.isPresent() && !oldClaim.get().getSetting("fluid_crosses_borders")) ||
                (newClaim.isPresent() && !newClaim.get().getSetting("fluid_crosses_borders"))) && !newClaim.equals(oldClaim)) {
            ci.cancel();
        }
    }

}
