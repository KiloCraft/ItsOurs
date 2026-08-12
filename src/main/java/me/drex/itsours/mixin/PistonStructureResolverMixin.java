package me.drex.itsours.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import me.drex.itsours.claim.AbstractClaim;
import me.drex.itsours.claim.list.ClaimList;
import me.drex.itsours.claim.flags.Flags;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.piston.PistonStructureResolver;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Optional;

@Mixin(PistonStructureResolver.class)
public abstract class PistonStructureResolverMixin {

    @Shadow
    @Final
    private BlockPos pistonPos;

    @Shadow
    @Final
    private Level level;

    @Shadow
    @Final
    private Direction pushDirection;

    @WrapOperation(
        method = "resolve",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/level/block/piston/PistonBaseBlock;isPushable(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/core/Direction;ZLnet/minecraft/core/Direction;)Z"
        )
    )
    private boolean itsours$canMove(BlockState state, Level world, BlockPos pos, Direction direction, boolean canBreak, Direction pistonDir, Operation<Boolean> original) {
        return handleMoveOperation(state, world, pos, direction, canBreak, pistonDir, original);
    }

    @WrapOperation(
        method = "addBlockLine",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/level/block/piston/PistonBaseBlock;isPushable(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/core/Direction;ZLnet/minecraft/core/Direction;)Z"
        )
    )
    private boolean itsours$canMove2(BlockState state, Level world, BlockPos pos, Direction direction, boolean canBreak, Direction pistonDir, Operation<Boolean> original) {
        return handleMoveOperation(state, world, pos, direction, canBreak, pistonDir, original);
    }

    private boolean handleMoveOperation(BlockState state, Level world, BlockPos pos, Direction direction, boolean canBreak, Direction pistonDir, Operation<Boolean> original) {
        Optional<AbstractClaim> oldClaim = ClaimList.getClaimAt(this.level, this.pistonPos);
        Optional<AbstractClaim> newClaim = ClaimList.getClaimAt(this.level, pos.relative(pushDirection));
        if (((oldClaim.isPresent() && !oldClaim.get().checkAction(null, Flags.PISTON_CROSSES_BORDERS)) ||
            (newClaim.isPresent() && !newClaim.get().checkAction(null, Flags.PISTON_CROSSES_BORDERS))) && !newClaim.equals(oldClaim)) {
            return false;
        }
        return original.call(state, world, pos, direction, canBreak, pistonDir);
    }

}
