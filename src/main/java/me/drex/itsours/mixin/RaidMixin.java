package me.drex.itsours.mixin;

import me.drex.itsours.ItsOursMod;
import me.drex.itsours.claim.AbstractClaim;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.village.raid.Raid;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Optional;

@Mixin(Raid.class)
public abstract class RaidMixin {


    @Shadow public abstract void invalidate();

    @Shadow @Final private ServerWorld world;

    @Inject(method = "spawnNextWave", at = @At(value = "HEAD"))
    public void canRaidSpawninClaim(BlockPos pos, CallbackInfo ci) {
        Optional<AbstractClaim> claim = ItsOursMod.INSTANCE.getClaimList().get(this.world, pos);
        if (claim.isPresent() && !claim.get().getSetting("mobspawn")) {
            this.invalidate();
            ci.cancel();
        }
    }

}
