package me.drex.itsours.mixin;

import me.drex.itsours.ItsOursMod;
import me.drex.itsours.claim.AbstractClaim;
import me.drex.itsours.claim.permission.util.Permission;
import me.drex.itsours.user.ClaimPlayer;
import me.drex.itsours.util.Color;
import net.kyori.adventure.text.Component;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BoatItem;
import net.minecraft.item.Item;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(BoatItem.class)
public class BoatItemMixin extends Item {

    public BoatItemMixin(Settings settings) {
        super(settings);
    }

    @Redirect(method = "use", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/BoatItem;raycast(Lnet/minecraft/world/World;Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/world/RaycastContext$FluidHandling;)Lnet/minecraft/util/hit/BlockHitResult;"))
    private BlockHitResult itsours$onBucketUse(World world, PlayerEntity player, RaycastContext.FluidHandling fluidHandling) {
        BlockHitResult hit = Item.raycast(world, player, fluidHandling);
        AbstractClaim claim = ItsOursMod.INSTANCE.getClaimList().get((ServerWorld) world, hit.getBlockPos());
        if (claim == null)
            return hit;
        if (!claim.hasPermission(player.getUuid(), "interact_block." + Permission.toString(this))) {
            ClaimPlayer claimPlayer = (ClaimPlayer) player;
            claimPlayer.sendError(Component.text("You can't interact with that block here.").color(Color.RED));
            return BlockHitResult.createMissed( hit.getPos(), hit.getSide(), hit.getBlockPos());
        }
        return hit;
    }

}
