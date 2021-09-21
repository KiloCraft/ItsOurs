package me.drex.itsours.mixin;

import me.drex.itsours.ItsOursMod;
import me.drex.itsours.claim.AbstractClaim;
import net.minecraft.block.BlockState;
import net.minecraft.entity.EntityType;
import net.minecraft.fluid.FluidState;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;
import net.minecraft.world.SpawnHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

@Mixin(SpawnHelper.class)
public abstract class SpawnHelperMixin {

    @Inject(
            method = "isClearForSpawn",
            at = @At("HEAD"),
            cancellable = true
    )
    private static void canPhantomsSpawn(BlockView blockView, BlockPos pos, BlockState state, FluidState fluidState, EntityType<?> entityType, CallbackInfoReturnable<Boolean> cir) {
        if (blockView instanceof ServerWorld) {
            if (ItsOursMod.INSTANCE == null || ItsOursMod.INSTANCE.getClaimList() == null) return;
            Optional<AbstractClaim> claim = ItsOursMod.INSTANCE.getClaimList().get((ServerWorld) blockView, pos);
            if (claim.isPresent() && !claim.get().getSetting("mobspawn") && entityType.equals(EntityType.PHANTOM)) {
                cir.setReturnValue(false);
            }
        }
    }

}
