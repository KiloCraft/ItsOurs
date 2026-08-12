package me.drex.itsours.mixin;

import me.drex.itsours.claim.AbstractClaim;
import me.drex.itsours.claim.list.ClaimList;
import me.drex.itsours.claim.flags.Flags;
import me.drex.itsours.claim.flags.node.Node;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.Optional;

@Mixin(targets = "net.minecraft.world.entity.monster.EnderMan$EndermanTakeBlockGoal")
public abstract class PickUpBlockGoalMixin {

    @Shadow
    @Final
    private EnderMan enderman;

    @Inject(
        method = "tick",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/level/Level;getBlockState(Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/block/state/BlockState;"
        ),
        locals = LocalCapture.CAPTURE_FAILHARD,
        cancellable = true
    )
    public void itsours$canEndermanPickUp(CallbackInfo ci, RandomSource random, Level world, int i, int j, int k, BlockPos pos) {
        Optional<AbstractClaim> optional = ClaimList.getClaimAt(world, pos);
        if (optional.isPresent()) {
            if (!optional.get().checkAction(this.enderman.getUUID(), Flags.MINE, Node.registry(BuiltInRegistries.BLOCK, world.getBlockState(pos).getBlock()))) {
                ci.cancel();
            }
        }
    }

}
