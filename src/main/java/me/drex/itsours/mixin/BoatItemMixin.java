package me.drex.itsours.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import me.drex.itsours.claim.AbstractClaim;
import me.drex.itsours.claim.ClaimList;
import me.drex.itsours.claim.permission.PermissionManager;
import me.drex.itsours.claim.permission.node.Node;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BoatItem;
import net.minecraft.item.Item;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.registry.Registries;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Optional;

@Mixin(BoatItem.class)
public abstract class BoatItemMixin extends Item {

    public BoatItemMixin(Settings settings) {
        super(settings);
    }

    @ModifyExpressionValue(
            method = "use",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/item/BoatItem;raycast(Lnet/minecraft/world/World;Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/world/RaycastContext$FluidHandling;)Lnet/minecraft/util/hit/BlockHitResult;"
            )
    )
    private BlockHitResult itsours$canUseBoat(BlockHitResult original, World world, PlayerEntity player) {
        Optional<AbstractClaim> claim = ClaimList.INSTANCE.getClaimAt((ServerWorld) world, original.getBlockPos());
        if (claim.isEmpty())
            return original;
        if (!claim.get().hasPermission(player.getUuid(), PermissionManager.USE_ITEM, Node.dummy(Registries.ITEM, this))) {
            player.sendMessage(Text.translatable("text.itsours.action.disallowed.interact_item").formatted(Formatting.RED), true);
            return BlockHitResult.createMissed(original.getPos(), original.getSide(), original.getBlockPos());
        }
        return original;
    }

}
