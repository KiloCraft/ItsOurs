package me.drex.itsours.mixin;

import me.drex.itsours.claim.AbstractClaim;
import me.drex.itsours.claim.ClaimList;
import me.drex.itsours.claim.permission.PermissionManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.SpawnRestriction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.ServerWorldAccess;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

@Mixin(SpawnRestriction.class)
public abstract class SpawnRestrictionMixin {

    @Inject(
            method = "canSpawn",
            at = @At("HEAD"),
            cancellable = true
    )
    private static <T extends Entity> void canMobsSpawn(EntityType<T> type, ServerWorldAccess world, SpawnReason spawnReason, BlockPos pos, Random random, CallbackInfoReturnable<Boolean> cir) {
        Optional<AbstractClaim> claim = ClaimList.INSTANCE.getClaimAt(world.toServerWorld(), pos);
        if (claim.isPresent() && !claim.get().hasPermission(null, PermissionManager.MOB_SPAWN)) {
            cir.setReturnValue(false);
        }
    }

}
