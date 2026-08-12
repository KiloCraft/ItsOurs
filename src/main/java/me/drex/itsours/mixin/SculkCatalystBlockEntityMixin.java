package me.drex.itsours.mixin;

import com.llamalad7.mixinextras.injector.WrapWithCondition;
import me.drex.itsours.claim.AbstractClaim;
import me.drex.itsours.claim.list.ClaimList;
import me.drex.itsours.claim.flags.Flags;
import net.minecraft.world.level.block.entity.SculkCatalystBlockEntity;
import net.minecraft.world.level.block.SculkSpreader;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.gameevent.PositionSource;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Optional;

@Mixin(SculkCatalystBlockEntity.CatalystListener.class)
public abstract class SculkCatalystBlockEntityMixin {

    @Shadow
    @Final
    private PositionSource positionSource;

    @WrapWithCondition(
        method = "handleGameEvent",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/level/block/SculkSpreader;addCursors(Lnet/minecraft/core/BlockPos;I)V"
        )
    )
    public boolean itsours$dontSpreadFromOtherSculkCatalyst(SculkSpreader sculkSpreadManager, BlockPos pos, int charge, ServerLevel world) {
        BlockPos oldPos = BlockPos.containing(this.positionSource.getPosition(world).orElse(Vec3.ZERO));
        Optional<AbstractClaim> oldClaim = ClaimList.getClaimAt(world, oldPos);
        Optional<AbstractClaim> newClaim = ClaimList.getClaimAt(world, pos);
        return ((oldClaim.isEmpty() || oldClaim.get().checkAction(null, Flags.SCULK_CROSSES_BORDERS)) &&
            (newClaim.isEmpty() || newClaim.get().checkAction(null, Flags.SCULK_CROSSES_BORDERS))) || newClaim.equals(oldClaim);
    }

}
