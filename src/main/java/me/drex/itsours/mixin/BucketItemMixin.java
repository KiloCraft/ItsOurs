package me.drex.itsours.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import me.drex.itsours.claim.AbstractClaim;
import me.drex.itsours.claim.list.ClaimList;
import me.drex.itsours.claim.flags.Flags;
import me.drex.itsours.claim.flags.node.Node;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BucketItem;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Optional;

import static me.drex.message.api.LocalizedMessage.localized;

@Mixin(BucketItem.class)
public abstract class BucketItemMixin extends Item {

    public BucketItemMixin(Settings settings) {
        super(settings);
    }

    @ModifyExpressionValue(
        method = "use",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/item/BucketItem;raycast(Lnet/minecraft/world/World;Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/world/RaycastContext$FluidHandling;)Lnet/minecraft/util/hit/BlockHitResult;"
        )
    )
    private BlockHitResult itsours$canUseBucket(BlockHitResult original, World world, PlayerEntity player) {
        Optional<AbstractClaim> claim = ClaimList.getClaimAt(world, original.getBlockPos());
        if (claim.isEmpty())
            return original;
        if (!claim.get().checkAction(player.getUuid(), Flags.USE_ITEM, Node.registry(Registries.ITEM, this))) {
            player.sendMessage(localized("text.itsours.action.disallowed.interact_item"), true);
            return BlockHitResult.createMissed(original.getPos(), original.getSide(), original.getBlockPos());
        }
        return original;
    }

}
