package me.drex.itsours.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import me.drex.itsours.claim.AbstractClaim;
import me.drex.itsours.claim.flags.Flags;
import me.drex.itsours.claim.list.ClaimList;
import net.minecraft.block.BlockState;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;
import net.minecraft.world.explosion.Explosion;
import net.minecraft.world.explosion.ExplosionBehavior;
import net.minecraft.world.explosion.ExplosionImpl;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Optional;

@Mixin(value = ExplosionImpl.class, priority = 500)
public abstract class ExplosionImplMixin {

    @Shadow
    @Final
    private ServerWorld world;

    @WrapOperation(
        method = "getBlocksToDestroy",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/explosion/ExplosionBehavior;canDestroyBlock(Lnet/minecraft/world/explosion/Explosion;Lnet/minecraft/world/BlockView;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;F)Z"
        )
    )
    public boolean itsours$canDestroyBlock(ExplosionBehavior instance, Explosion explosion, BlockView world, BlockPos pos, BlockState state, float power, Operation<Boolean> original) {
        Optional<AbstractClaim> claim = ClaimList.getClaimAt(this.world, pos);
        if (claim.isPresent() && !claim.get().checkAction(null, Flags.EXPLOSIONS)) {
            return false;
        }
        return original.call(instance, explosion, world, pos, state, power);
    }

}
