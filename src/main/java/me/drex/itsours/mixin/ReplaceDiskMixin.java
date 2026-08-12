package me.drex.itsours.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import me.drex.itsours.claim.AbstractClaim;
import me.drex.itsours.claim.flags.Flags;
import me.drex.itsours.claim.flags.node.Node;
import me.drex.itsours.claim.list.ClaimList;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.item.enchantment.EnchantedItemInUse;
import net.minecraft.world.item.enchantment.effects.ReplaceDisk;
import net.minecraft.world.entity.Entity;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Optional;

@Mixin(ReplaceDisk.class)
public abstract class ReplaceDiskMixin {

    @WrapOperation(
        method = "apply",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/core/BlockPos;distToCenterSqr(DDD)D"
        )
    )
    private double itsours$canFreezeWater(BlockPos pos, double x, double y, double z, Operation<Double> original, ServerLevel world, int level, EnchantedItemInUse context, Entity entity, Vec3 vec3d) {
        Optional<AbstractClaim> optional = ClaimList.getClaimAt(world, pos);
        Boolean canPlace = optional.map(claim -> claim.checkAction(entity.getUUID(), Flags.PLACE, Node.registry(BuiltInRegistries.BLOCK, Blocks.FROSTED_ICE.defaultBlockState().getBlock()))).orElse(true);
        if (canPlace) {
            return original.call(pos, x, y, z);
        } else {
            return Double.MAX_VALUE;
        }
    }

}
