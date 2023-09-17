package me.drex.itsours.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import me.drex.itsours.claim.AbstractClaim;
import me.drex.itsours.claim.ClaimList;
import me.drex.itsours.claim.permission.PermissionManager;
import me.drex.itsours.claim.permission.node.Node;
import me.drex.itsours.user.ClaimPlayer;
import net.minecraft.block.Block;
import net.minecraft.block.ChestBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.registry.Registries;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

import static me.drex.message.api.LocalizedMessage.localized;

@Mixin(BlockItem.class)
public abstract class BlockItemMixin extends Item {

    public BlockItemMixin(Settings settings) {
        super(settings);
    }

    @Shadow
    public abstract Block getBlock();

    @ModifyExpressionValue(
        method = "place(Lnet/minecraft/item/ItemPlacementContext;)Lnet/minecraft/util/ActionResult;",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/item/ItemPlacementContext;canPlace()Z"
        )
    )
    private boolean itsours$canPlace(boolean original, ItemPlacementContext context) {
        if (context.getPlayer() == null) return original;
        Optional<AbstractClaim> claim = ClaimList.getClaimAt(context);
        if (claim.isEmpty()) return original;
        if (!claim.get().hasPermission(context.getPlayer().getUuid(), PermissionManager.PLACE, Node.registry(Registries.BLOCK, this.getBlock()))) {
            context.getPlayer().sendMessage(localized("text.itsours.action.disallowed.place_block"), true);
            return false;
        }
        return original;
    }


    @Inject(
        method = "place(Lnet/minecraft/item/ItemPlacementContext;)Lnet/minecraft/util/ActionResult;",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/block/Block;onPlaced(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/item/ItemStack;)V"
        )
    )
    private void itsours$onBlockPlace(ItemPlacementContext context, CallbackInfoReturnable<ActionResult> cir) {
        if (context.getPlayer() == null) return;
        Block block = this.getBlock();
        BlockPos blockPos = context.getBlockPos();
        Optional<AbstractClaim> optionalClaim = ClaimList.getClaimAt(context);
        // TODO: Make configurable
        if ((block instanceof ChestBlock) && optionalClaim.isEmpty()) {
            PlayerEntity player = context.getPlayer();
            ClaimPlayer claimPlayer = (ClaimPlayer) player;
            if (claimPlayer != null && ClaimList.getClaimsFrom(player.getUuid()).isEmpty()) {
                player.sendMessage(localized("text.itsours.info.notProtected"));
                claimPlayer.setFirstPosition(new BlockPos(blockPos.getX() + 3, blockPos.getY(), blockPos.getZ() + 3));
                claimPlayer.setSecondPosition(new BlockPos(blockPos.getX() - 3, blockPos.getY(), blockPos.getZ() - 3));
            }
        }
    }

}
