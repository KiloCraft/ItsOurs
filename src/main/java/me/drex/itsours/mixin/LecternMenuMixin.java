package me.drex.itsours.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import me.drex.itsours.claim.AbstractClaim;
import me.drex.itsours.claim.list.ClaimList;
import me.drex.itsours.claim.flags.Flags;
import me.drex.itsours.claim.flags.node.Node;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.entity.player.Player;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.inventory.LecternMenu;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Optional;

import static me.drex.itsours.util.LocalizedMessage.localized;

@Mixin(LecternMenu.class)
public abstract class LecternMenuMixin {

    @ModifyExpressionValue(
        method = "clickMenuButton",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/entity/player/Player;mayBuild()Z"
        )
    )
    public boolean itsours$canTakeBook(boolean original, Player player) {
        Optional<AbstractClaim> claim = ClaimList.getClaimAt(player);
        if (claim.isEmpty())
            return original;
        if (!claim.get().checkAction(player.getUUID(), Flags.MINE, Node.registry(BuiltInRegistries.BLOCK, Blocks.LECTERN))) {
            player.displayClientMessage(localized("text.itsours.action.disallowed.interact_block", player), true);
            return false;
        }
        return original;
    }


}
