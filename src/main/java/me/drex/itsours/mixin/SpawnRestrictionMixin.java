package me.drex.itsours.mixin;

import me.drex.itsours.ItsOursMod;
import me.drex.itsours.claim.AbstractClaim;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.SpawnRestriction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.ServerWorldAccess;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;
import java.util.Random;

@Mixin(SpawnRestriction.class)
public abstract class SpawnRestrictionMixin {

    @Inject(
            method = "canSpawn",
            at = @At("HEAD"),
            cancellable = true
    )
    private static <T extends Entity> void canMobsSpawn(EntityType<T> type, ServerWorldAccess serverWorldAccess, SpawnReason spawnReason, BlockPos pos, Random random, CallbackInfoReturnable<Boolean> cir) {
        if (ItsOursMod.INSTANCE == null || ItsOursMod.INSTANCE.getClaimList() == null) return;
        Optional<AbstractClaim> claim = ItsOursMod.INSTANCE.getClaimList().get(serverWorldAccess.toServerWorld(), pos);
        if (claim.isPresent() && !claim.get().getSetting("mobspawn")) {
            cir.setReturnValue(false);
        }
    }

}
