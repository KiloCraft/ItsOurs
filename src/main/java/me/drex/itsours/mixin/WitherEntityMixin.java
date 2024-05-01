package me.drex.itsours.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import me.drex.itsours.claim.AbstractClaim;
import me.drex.itsours.claim.ClaimList;
import me.drex.itsours.claim.flags.FlagsManager;
import me.drex.itsours.claim.flags.node.Node;
import net.minecraft.entity.Entity;
import net.minecraft.entity.boss.WitherEntity;
import net.minecraft.registry.Registries;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Optional;

@Mixin(WitherEntity.class)
public abstract class WitherEntityMixin {

    @WrapOperation(
        method = "mobTick",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/World;breakBlock(Lnet/minecraft/util/math/BlockPos;ZLnet/minecraft/entity/Entity;)Z"
        )
    )
    private boolean itsours$canWitherBreakBlock(World world, BlockPos pos, boolean drop, Entity breakingEntity, Operation<Boolean> original) {
        Optional<AbstractClaim> optional = ClaimList.getClaimAt(world, pos);
        if (optional.isPresent()) {
            if (!optional.get().checkAction(breakingEntity.getUuid(), FlagsManager.MINE, Node.registry(Registries.BLOCK, world.getBlockState(pos).getBlock()))) {
                return false;
            }
        }
        return original.call(world, pos, drop, breakingEntity);
    }

}
