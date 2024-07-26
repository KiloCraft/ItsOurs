package me.drex.itsours.mixin;

import com.llamalad7.mixinextras.injector.WrapWithCondition;
import me.drex.itsours.claim.AbstractClaim;
import me.drex.itsours.claim.list.ClaimList;
import me.drex.itsours.claim.flags.Flags;
import net.minecraft.block.entity.SculkCatalystBlockEntity;
import net.minecraft.block.entity.SculkSpreadManager;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.event.PositionSource;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Optional;

@Mixin(SculkCatalystBlockEntity.Listener.class)
public abstract class SculkCatalystBlockEntityMixin {

    @Shadow
    @Final
    private PositionSource positionSource;

    @WrapWithCondition(
        method = "listen",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/block/entity/SculkSpreadManager;spread(Lnet/minecraft/util/math/BlockPos;I)V"
        )
    )
    public boolean itsours$dontSpreadFromOtherSculkCatalyst(SculkSpreadManager sculkSpreadManager, BlockPos pos, int charge, ServerWorld world) {
        BlockPos oldPos = BlockPos.ofFloored(this.positionSource.getPos(world).orElse(Vec3d.ZERO));
        Optional<AbstractClaim> oldClaim = ClaimList.getClaimAt(world, oldPos);
        Optional<AbstractClaim> newClaim = ClaimList.getClaimAt(world, pos);
        return ((oldClaim.isEmpty() || oldClaim.get().checkAction(null, Flags.SCULK_CROSSES_BORDERS)) &&
            (newClaim.isEmpty() || newClaim.get().checkAction(null, Flags.SCULK_CROSSES_BORDERS))) || newClaim.equals(oldClaim);
    }

}
