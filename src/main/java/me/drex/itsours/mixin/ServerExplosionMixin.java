package me.drex.itsours.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import me.drex.itsours.claim.AbstractClaim;
import me.drex.itsours.claim.flags.Flags;
import me.drex.itsours.claim.list.ClaimList;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.ExplosionDamageCalculator;
import net.minecraft.world.level.ServerExplosion;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Optional;

@Mixin(value = ServerExplosion.class, priority = 1500)
public abstract class ServerExplosionMixin {

    @Shadow
    @Final
    private ServerLevel level;

    @WrapOperation(
        method = {
            "calculateExplodedPositions",
            // lithium compat
            "tryMarkBlockForDestruction"
        },
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/level/ExplosionDamageCalculator;shouldBlockExplode(Lnet/minecraft/world/level/Explosion;Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;F)Z"
        )
    )
    public boolean itsours$canDestroyBlock(ExplosionDamageCalculator instance, Explosion explosion, BlockGetter world, BlockPos pos, BlockState state, float power, Operation<Boolean> original) {
        Optional<AbstractClaim> claim = ClaimList.getClaimAt(this.level, pos);
        if (claim.isPresent() && !claim.get().checkAction(null, Flags.EXPLOSIONS)) {
            return false;
        }
        return original.call(instance, explosion, world, pos, state, power);
    }

}
