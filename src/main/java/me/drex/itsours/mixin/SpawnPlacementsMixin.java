package me.drex.itsours.mixin;

import me.drex.itsours.claim.AbstractClaim;
import me.drex.itsours.claim.list.ClaimList;
import me.drex.itsours.claim.flags.Flags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ServerLevelAccessor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

@Mixin(SpawnPlacements.class)
public abstract class SpawnPlacementsMixin {

    @Inject(
        method = "checkSpawnRules",
        at = @At("HEAD"),
        cancellable = true
    )
    private static <T extends Entity> void itsours$canMobsSpawn(EntityType<T> type, ServerLevelAccessor world, EntitySpawnReason spawnReason, BlockPos pos, RandomSource random, CallbackInfoReturnable<Boolean> cir) {
        Optional<AbstractClaim> claim = ClaimList.getClaimAt(world.getLevel(), pos);
        if (claim.isPresent() && !claim.get().checkAction(null, Flags.MOB_SPAWN)) {
            cir.setReturnValue(false);
        }
    }

}
