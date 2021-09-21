package me.drex.itsours.mixin;

import me.drex.itsours.ItsOursMod;
import me.drex.itsours.claim.AbstractClaim;
import me.drex.itsours.user.ClaimPlayer;
import me.drex.itsours.util.Color;
import net.kyori.adventure.text.Component;
import net.minecraft.block.Block;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BucketItem;
import net.minecraft.item.Item;
import net.minecraft.server.world.ServerWorld;
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
        Optional<AbstractClaim> claim = ItsOursMod.INSTANCE.getClaimList().get((ServerWorld) world, hit.getBlockPos());
        if (!claim.isPresent())
            return hit;
        if (!claim.get().hasPermission(player.getUuid(), "use_item." + Registry.ITEM.getId(this).getPath())) {
            ClaimPlayer claimPlayer = (ClaimPlayer) player;
            claimPlayer.sendError(Component.text("You can't use that item here.").color(Color.RED));
            return BlockHitResult.createMissed(hit.getPos(), hit.getSide(), hit.getBlockPos());
        }
        return hit;
    }

}
