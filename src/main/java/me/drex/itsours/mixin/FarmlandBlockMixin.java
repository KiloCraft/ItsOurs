package me.drex.itsours.mixin;

import com.llamalad7.mixinextras.injector.WrapWithCondition;
import me.drex.itsours.claim.AbstractClaim;
import me.drex.itsours.claim.ClaimList;
import me.drex.itsours.claim.permission.PermissionManager;
import me.drex.itsours.claim.permission.node.Node;
import net.minecraft.block.BlockState;
import net.minecraft.block.FarmlandBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.Registries;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Optional;

import static me.drex.message.api.LocalizedMessage.localized;

@Mixin(FarmlandBlock.class)
public abstract class FarmlandBlockMixin {

    @WrapWithCondition(
        method = "onLandedUpon",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/block/FarmlandBlock;setToDirt(Lnet/minecraft/entity/Entity;Lnet/minecraft/block/BlockState;Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;)V")
    )
    private boolean itsours$canPlayerTrample(Entity entity, BlockState state, World world, BlockPos pos) {
        Optional<AbstractClaim> claim = ClaimList.getClaimAt((ServerWorld) world, pos);
        if (claim.isPresent() && entity instanceof PlayerEntity player && !claim.get().hasPermission(entity.getUuid(), PermissionManager.MINE, Node.registry(Registries.BLOCK, (FarmlandBlock) (Object) this))) {
            player.sendMessage(localized("text.itsours.action.disallowed.break_block"), true);
            return false;
        }
        return true;
    }

}
