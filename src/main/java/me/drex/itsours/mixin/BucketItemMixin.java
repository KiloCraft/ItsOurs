package me.drex.itsours.mixin;

import me.drex.itsours.claim.AbstractClaim;
import me.drex.itsours.claim.ClaimList;
import me.drex.itsours.claim.permission.PermissionManager;
import me.drex.itsours.claim.permission.node.Node;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BucketItem;
import net.minecraft.item.Item;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Optional;

@Mixin(BucketItem.class)
public abstract class BucketItemMixin extends Item {

    public BucketItemMixin(Settings settings) {
        super(settings);
    }

    @Redirect(
            method = "use",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/item/BucketItem;raycast(Lnet/minecraft/world/World;Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/world/RaycastContext$FluidHandling;)Lnet/minecraft/util/hit/BlockHitResult;"
            )
    )
    private BlockHitResult canUseBucket(World world, PlayerEntity player, RaycastContext.FluidHandling fluidHandling) {
        BlockHitResult hit = Item.raycast(world, player, fluidHandling);
        Optional<AbstractClaim> claim = ClaimList.INSTANCE.getClaimAt((ServerWorld) world, hit.getBlockPos());
        if (claim.isEmpty())
            return hit;
        if (!claim.get().hasPermission(player.getUuid(), PermissionManager.USE_ITEM, Node.dummy(Registry.ITEM, this))) {
            player.sendMessage(Text.translatable("text.itsours.action.disallowed.interact_item").formatted(Formatting.RED));
            return BlockHitResult.createMissed(hit.getPos(), hit.getSide(), hit.getBlockPos());
        }
        return hit;
    }

}
