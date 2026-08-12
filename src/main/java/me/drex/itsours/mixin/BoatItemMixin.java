package me.drex.itsours.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import me.drex.itsours.claim.AbstractClaim;
import me.drex.itsours.claim.list.ClaimList;
import me.drex.itsours.claim.flags.Flags;
import me.drex.itsours.claim.flags.node.Node;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BoatItem;
import net.minecraft.world.item.Item;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Optional;

import static me.drex.itsours.util.LocalizedMessage.localized;
import static me.drex.itsours.util.LocalizedMessage.sendMessage;

@Mixin(BoatItem.class)
public abstract class BoatItemMixin extends Item {

    public BoatItemMixin(Properties settings) {
        super(settings);
    }

    @ModifyExpressionValue(
        method = "use",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/item/BoatItem;getPlayerPOVHitResult(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/level/ClipContext$Fluid;)Lnet/minecraft/world/phys/BlockHitResult;"
        )
    )
    private BlockHitResult itsours$canUseBoat(BlockHitResult original, Level world, Player player) {
        Optional<AbstractClaim> claim = ClaimList.getClaimAt(world, original.getBlockPos());
        if (claim.isEmpty())
            return original;
        if (!claim.get().checkAction(player.getUUID(), Flags.USE_ITEM, Node.registry(BuiltInRegistries.ITEM, this))) {
            sendMessage(player, localized("text.itsours.action.disallowed.interact_item", player), true);
            return BlockHitResult.miss(original.getLocation(), original.getDirection(), original.getBlockPos());
        }
        return original;
    }

}
