package me.drex.itsours.mixin;

import me.drex.itsours.ItsOursMod;
import me.drex.itsours.claim.AbstractClaim;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.explosion.Explosion;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.ListIterator;
import java.util.Optional;

@Mixin(Explosion.class)
public abstract class ExplosionMixin {

    @Shadow
    @Final
    private World world;

    @Shadow
    @Final
    private List<BlockPos> affectedBlocks;

    @Inject(
            method = "affectWorld",
            at = @At("HEAD")
    )
    public void canExplosionAffectBlock(boolean bl, CallbackInfo ci) {
        ListIterator<BlockPos> iterator = this.affectedBlocks.listIterator();
        while (iterator.hasNext()) {
            BlockPos blockPos = iterator.next();
            Optional<AbstractClaim> claim = ItsOursMod.INSTANCE.getClaimList().get((ServerWorld) this.world, blockPos);
            if (claim.isPresent() && !claim.get().getSetting("explosions")) {
                iterator.remove();
            }
        }
    }

}
