package me.drex.itsours.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import me.drex.itsours.claim.AbstractClaim;
import me.drex.itsours.claim.list.ClaimList;
import me.drex.itsours.claim.flags.Flags;
import me.drex.itsours.claim.flags.node.Node;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Optional;

@Mixin(WitherBoss.class)
public abstract class WitherBossMixin {

    @WrapOperation(
        method = "customServerAiStep",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/server/level/ServerLevel;destroyBlock(Lnet/minecraft/core/BlockPos;ZLnet/minecraft/world/entity/Entity;)Z"
        )
    )
    private boolean itsours$canWitherBreakBlock(ServerLevel world, BlockPos pos, boolean drop, Entity breakingEntity, Operation<Boolean> original) {
        Optional<AbstractClaim> optional = ClaimList.getClaimAt(world, pos);
        if (optional.isPresent()) {
            if (!optional.get().checkAction(breakingEntity.getUUID(), Flags.MINE, Node.registry(BuiltInRegistries.BLOCK, world.getBlockState(pos).getBlock()))) {
                return false;
            }
        }
        return original.call(world, pos, drop, breakingEntity);
    }

}
