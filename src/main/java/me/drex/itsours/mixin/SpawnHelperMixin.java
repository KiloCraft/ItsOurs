package me.drex.itsours.mixin;

import me.drex.itsours.claim.AbstractClaim;
import me.drex.itsours.claim.ClaimList;
import me.drex.itsours.claim.permission.PermissionManager;
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
            Optional<AbstractClaim> claim = ClaimList.INSTANCE.getClaimAt((ServerWorld) blockView, pos);
            if (claim.isPresent() && !claim.get().hasPermission(null, PermissionManager.MOB_SPAWN) && entityType.equals(EntityType.PHANTOM)) {
                cir.setReturnValue(false);
            }
        }
    }

}
