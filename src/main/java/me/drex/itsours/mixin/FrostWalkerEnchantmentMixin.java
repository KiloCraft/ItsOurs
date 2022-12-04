package me.drex.itsours.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import me.drex.itsours.claim.AbstractClaim;
import me.drex.itsours.claim.ClaimList;
import me.drex.itsours.claim.permission.PermissionManager;
import me.drex.itsours.claim.permission.node.Node;
import net.minecraft.block.Blocks;
import net.minecraft.enchantment.FrostWalkerEnchantment;
import net.minecraft.entity.LivingEntity;
import net.minecraft.registry.Registries;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Optional;

@Mixin(FrostWalkerEnchantment.class)
public abstract class FrostWalkerEnchantmentMixin {

    @ModifyExpressionValue(
            method = "freezeWater",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/block/BlockState;canPlaceAt(Lnet/minecraft/world/WorldView;Lnet/minecraft/util/math/BlockPos;)Z"
            )
    )
    private static boolean itsours$canFreezeWater(boolean original, LivingEntity entity, World world, BlockPos pos, int level) {
        Optional<AbstractClaim> optional = ClaimList.INSTANCE.getClaimAt((ServerWorld) world, pos);
        return optional.map(claim -> claim.hasPermission(entity.getUuid(), PermissionManager.PLACE, Node.dummy(Registries.BLOCK, Blocks.FROSTED_ICE.getDefaultState().getBlock())) && original).orElse(original);
    }

}