package me.drex.itsours.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import me.drex.itsours.claim.AbstractClaim;
import me.drex.itsours.claim.list.ClaimList;
import me.drex.itsours.claim.flags.Flags;
import me.drex.itsours.claim.flags.node.Node;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.TurtleEggBlock;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Optional;

import static me.drex.itsours.util.LocalizedMessage.localized;
import static me.drex.itsours.util.LocalizedMessage.sendMessage;

@Mixin(TurtleEggBlock.class)
public abstract class TurtleEggBlockMixin {

    @ModifyExpressionValue(
        method = "destroyEgg",
        at = @At(
            value = "INVOKE", target = "Lnet/minecraft/world/level/block/TurtleEggBlock;canDestroyEgg(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/entity/Entity;)Z"
        )
    )
    public boolean itsours$canPlayerBreakEgg(boolean original, Level world, BlockState state, BlockPos pos, Entity entity) {
        Optional<AbstractClaim> claim = ClaimList.getClaimAt(world, pos);
        if (claim.isPresent() && entity instanceof Player player && !claim.get().checkAction(entity.getUUID(), Flags.MINE, Node.registry(BuiltInRegistries.BLOCK, (TurtleEggBlock) (Object) this))) {
            sendMessage(player, localized("text.itsours.action.disallowed.break_block", player), true);
            return false;
        }
        return original;
    }

}
