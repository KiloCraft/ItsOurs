package me.drex.itsours.mixin;

import me.drex.itsours.claim.AbstractClaim;
import me.drex.itsours.claim.list.ClaimList;
import me.drex.itsours.claim.flags.FlagsManager;
import me.drex.itsours.claim.flags.node.Node;
import net.minecraft.entity.mob.EndermanEntity;
import net.minecraft.registry.Registries;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.Optional;

@Mixin(targets = "net.minecraft.entity.mob.EndermanEntity$PickUpBlockGoal")
public abstract class PickUpBlockGoalMixin {

    @Shadow
    @Final
    private EndermanEntity enderman;

    @Inject(
        method = "tick",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/World;getBlockState(Lnet/minecraft/util/math/BlockPos;)Lnet/minecraft/block/BlockState;"
        ),
        locals = LocalCapture.CAPTURE_FAILHARD,
        cancellable = true
    )
    public void itsours$canEndermanPickUp(CallbackInfo ci, Random random, World world, int i, int j, int k, BlockPos pos) {
        Optional<AbstractClaim> optional = ClaimList.getClaimAt(world, pos);
        if (optional.isPresent()) {
            if (!optional.get().checkAction(this.enderman.getUuid(), FlagsManager.MINE, Node.registry(Registries.BLOCK, world.getBlockState(pos).getBlock()))) {
                ci.cancel();
            }
        }
    }

}
