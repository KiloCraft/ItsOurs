package me.drex.itsours.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import me.drex.itsours.claim.AbstractClaim;
import me.drex.itsours.claim.list.ClaimList;
import me.drex.itsours.claim.flags.Flags;
import net.minecraft.block.BlockState;
import net.minecraft.block.piston.PistonHandler;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Optional;

@Mixin(PistonHandler.class)
public abstract class PistonHandlerMixin {

    @Shadow
    @Final
    private BlockPos posFrom;

    @Shadow
    @Final
    private World world;

    @Shadow
    @Final
    private Direction motionDirection;

    @WrapOperation(
        method = "calculatePush",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/block/PistonBlock;isMovable(Lnet/minecraft/block/BlockState;Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/math/Direction;ZLnet/minecraft/util/math/Direction;)Z"
        )
    )
    private boolean itsours$canMove(BlockState state, World world, BlockPos pos, Direction direction, boolean canBreak, Direction pistonDir, Operation<Boolean> original) {
        return handleMoveOperation(state, world, pos, direction, canBreak, pistonDir, original);
    }

    @WrapOperation(
        method = "tryMove",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/block/PistonBlock;isMovable(Lnet/minecraft/block/BlockState;Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/math/Direction;ZLnet/minecraft/util/math/Direction;)Z"
        )
    )
    private boolean itsours$canMove2(BlockState state, World world, BlockPos pos, Direction direction, boolean canBreak, Direction pistonDir, Operation<Boolean> original) {
        return handleMoveOperation(state, world, pos, direction, canBreak, pistonDir, original);
    }

    private boolean handleMoveOperation(BlockState state, World world, BlockPos pos, Direction direction, boolean canBreak, Direction pistonDir, Operation<Boolean> original) {
        Optional<AbstractClaim> oldClaim = ClaimList.getClaimAt(this.world, this.posFrom);
        Optional<AbstractClaim> newClaim = ClaimList.getClaimAt(this.world, pos.offset(motionDirection));
        if (((oldClaim.isPresent() && !oldClaim.get().checkAction(null, Flags.PISTON_CROSSES_BORDERS)) ||
            (newClaim.isPresent() && !newClaim.get().checkAction(null, Flags.PISTON_CROSSES_BORDERS))) && !newClaim.equals(oldClaim)) {
            return false;
        }
        return original.call(state, world, pos, direction, canBreak, pistonDir);
    }

}
