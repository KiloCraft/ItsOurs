package me.drex.itsours.mixin;

import me.drex.itsours.claim.AbstractClaim;
import me.drex.itsours.claim.ClaimList;
import me.drex.itsours.claim.permission.PermissionManager;
import me.drex.itsours.claim.permission.node.Node;
import net.minecraft.block.BlockState;
import net.minecraft.enchantment.FrostWalkerEnchantment;
import net.minecraft.entity.LivingEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.WorldView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Optional;

@Mixin(FrostWalkerEnchantment.class)
public abstract class FrostWalkerEnchantmentMixin {

    @Redirect(
            method = "freezeWater",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/block/BlockState;canPlaceAt(Lnet/minecraft/world/WorldView;Lnet/minecraft/util/math/BlockPos;)Z"
            )
    )
    private static boolean canFreezeWater(BlockState blockState, WorldView world, BlockPos pos, LivingEntity livingEntity) {
        Optional<AbstractClaim> claim = ClaimList.INSTANCE.getClaimAt((ServerWorld) world, pos);
        return claim.isEmpty() || !(livingEntity instanceof ServerPlayerEntity) || claim.get().hasPermission(livingEntity.getUuid(), PermissionManager.PLACE, Node.dummy(Registry.BLOCK, blockState.getBlock()));
    }

}