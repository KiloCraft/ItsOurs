package me.drex.itsours.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import me.drex.itsours.claim.AbstractClaim;
import me.drex.itsours.claim.flags.FlagsManager;
import me.drex.itsours.claim.flags.node.Node;
import me.drex.itsours.claim.list.ClaimList;
import net.minecraft.block.Blocks;
import net.minecraft.enchantment.EnchantmentEffectContext;
import net.minecraft.enchantment.effect.entity.ReplaceDiscEnchantmentEffectType;
import net.minecraft.entity.Entity;
import net.minecraft.registry.Registries;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Optional;

@Mixin(ReplaceDiscEnchantmentEffectType.class)
public abstract class ReplaceDiscEnchantmentEffectTypeMixin {

    @WrapOperation(
        method = "apply",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/util/math/BlockPos;getSquaredDistanceFromCenter(DDD)D"
        )
    )
    private double itsours$canFreezeWater(BlockPos pos, double x, double y, double z, Operation<Double> original, ServerWorld world, int level, EnchantmentEffectContext context, Entity entity, Vec3d vec3d) {
        Optional<AbstractClaim> optional = ClaimList.getClaimAt(world, pos);
        Boolean canPlace = optional.map(claim -> claim.checkAction(entity.getUuid(), FlagsManager.PLACE, Node.registry(Registries.BLOCK, Blocks.FROSTED_ICE.getDefaultState().getBlock()))).orElse(true);
        if (canPlace) {
            return original.call(pos, x, y, z);
        } else {
            return Double.MAX_VALUE;
        }
    }

}
