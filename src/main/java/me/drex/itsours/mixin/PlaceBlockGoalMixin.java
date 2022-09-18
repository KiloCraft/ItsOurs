package me.drex.itsours.mixin;

import me.drex.itsours.claim.AbstractClaim;
import me.drex.itsours.claim.ClaimList;
import me.drex.itsours.claim.permission.PermissionManager;
import me.drex.itsours.claim.permission.node.Node;
import net.minecraft.block.BlockState;
import net.minecraft.entity.mob.EndermanEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

@Mixin(targets = "net.minecraft.entity.mob.EndermanEntity$PlaceBlockGoal")
public abstract class PlaceBlockGoalMixin {

    @Shadow
    @Final
    private EndermanEntity enderman;

    @Inject(method = "canPlaceOn", at = @At("HEAD"), cancellable = true)
    public void itsours$canEndermanPlace(World world, BlockPos posAbove, BlockState carriedState, BlockState stateAbove, BlockState state, BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        Optional<AbstractClaim> optional = ClaimList.INSTANCE.getClaimAt((ServerWorld) world, pos);
        if (optional.isPresent()) {
            if (!optional.get().hasPermission(this.enderman.getUuid(), PermissionManager.MINE, Node.dummy(Registry.BLOCK, world.getBlockState(pos).getBlock()))) {
                cir.setReturnValue(false);
            }
        }
    }

}

