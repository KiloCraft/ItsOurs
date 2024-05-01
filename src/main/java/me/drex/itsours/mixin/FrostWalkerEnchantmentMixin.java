package me.drex.itsours.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import me.drex.itsours.claim.AbstractClaim;
import me.drex.itsours.claim.ClaimList;
import me.drex.itsours.claim.flags.FlagsManager;
import me.drex.itsours.claim.flags.node.Node;
import net.minecraft.block.Blocks;
import net.minecraft.enchantment.FrostWalkerEnchantment;
import net.minecraft.entity.LivingEntity;
import net.minecraft.registry.Registries;
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
        Optional<AbstractClaim> optional = ClaimList.getClaimAt(world, pos);
        return optional.map(claim -> claim.checkAction(entity.getUuid(), FlagsManager.PLACE, Node.registry(Registries.BLOCK, Blocks.FROSTED_ICE.getDefaultState().getBlock())) && original).orElse(original);
    }

}
