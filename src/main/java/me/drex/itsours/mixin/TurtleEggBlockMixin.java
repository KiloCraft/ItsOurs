package me.drex.itsours.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import me.drex.itsours.claim.AbstractClaim;
import me.drex.itsours.claim.ClaimList;
import me.drex.itsours.claim.permission.PermissionManager;
import me.drex.itsours.claim.permission.node.Node;
import net.minecraft.block.BlockState;
import net.minecraft.block.TurtleEggBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.Registries;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Optional;

import static me.drex.message.api.LocalizedMessage.localized;

@Mixin(TurtleEggBlock.class)
public abstract class TurtleEggBlockMixin {

    @ModifyExpressionValue(
        method = "tryBreakEgg",
        at = @At(
            value = "INVOKE", target = "Lnet/minecraft/block/TurtleEggBlock;breaksEgg(Lnet/minecraft/world/World;Lnet/minecraft/entity/Entity;)Z"
        )
    )
    public boolean itsours$canPlayerBreakEgg(boolean original, World world, BlockState state, BlockPos pos, Entity entity) {
        Optional<AbstractClaim> claim = ClaimList.getClaimAt(world, pos);
        if (claim.isPresent() && entity instanceof PlayerEntity player && !claim.get().hasPermission(entity.getUuid(), PermissionManager.MINE, Node.registry(Registries.BLOCK, (TurtleEggBlock) (Object) this))) {
            player.sendMessage(localized("text.itsours.action.disallowed.break_block"), true);
            return false;
        }
        return original;
    }

}
